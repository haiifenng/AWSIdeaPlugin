package com.actionsoft.ideaplugins.dependencies;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.actionsoft.ideaplugins.helper.PluginConst;
import com.actionsoft.ideaplugins.helper.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.ModuleOrderEntryImpl;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Processor;

/**
 * Module的依赖更新
 *
 * @author Haiifenng
 * @date 2017.05.07
 */
public class AWSModuleDependencies {
	private Project project;

	public AWSModuleDependencies(Project project) {
		this.project = project;
	}

	/**
	 * 更新当前Project的所有Module的依赖，主入口
	 *
	 * @return
	 */
	public String updateDependencies() {
		Collection<Module> modules = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);
		StringBuilder message = new StringBuilder();
		AWSModuleDependencies awsModuleDependencies = new AWSModuleDependencies(project);
		for (Module module : modules) {
			String msg = awsModuleDependencies.updateDependencies(module);
			if (!StringUtil.isEmpty(msg)) {
				message.append(msg).append("\n");
			}
		}

		if (!StringUtil.isEmpty(message)) {
			message.setLength(message.toString().length() - 1);
			return String.format("更新了以下Module：\n%s", message.toString());
		} else {
			return "没有需要更新的Module";
		}
	}

	/**
	 * 更新指定的module的依赖
	 *
	 * @param module
	 * @return
	 */
	public String updateDependencies(Module module) {
		if (PluginUtil.isExcludeModule(module.getName())) {
			return "";
		} else if (PluginUtil.isAWSWebModule(module.getName())) {
			return updateWebModuleDependencies(module);
		} else if (module.getName().equals("aws-platform-upgrade")) {
			return updateUpgradeModuleDependencies(module);
		} else {
			return updateCommonModuleDependencies(module);
		}
	}

	private String updateCommonModuleDependencies(Module module) {
		Module[] modules = ModuleManager.getInstance(project).getSortedModules();
		ModuleWithDependenciesScope scope = (ModuleWithDependenciesScope) module.getModuleWithDependenciesAndLibrariesScope(true);
		final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
		//		modifiableModel.clear();
		for (Module tmpModule : modules) {
			String moduleTypeName = tmpModule.getModuleTypeName();
			if (!StdModuleTypes.JAVA.getId().equals(moduleTypeName)) {
				continue;
			}
			String name = tmpModule.getName();
			if (PluginUtil.isAWSWebModule(name)) {
				continue;
			}
			if (PluginUtil.isExcludeModule(name) || name.equals("aws-platform-upgrade")) {
				continue;
			}
			if (!scope.isSearchInModuleContent(tmpModule)) {//判断是否在依赖列表，没有的话加入
				modifiableModel.addModuleOrderEntry(tmpModule);
			}
		}

		final LibraryTable projectTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
		Library aws_lib = projectTable.getLibraryByName(PluginConst.PLUGIN_AWS_LIBRARY_NAME);
		if (aws_lib == null) {//判断当前project有没有aws_lib这个库
			Messages.showMessageDialog(project, "当前Project没有“aws_lib”的库，请使用“Tools->AWS Libraries 更新”菜单更新", "提示", Messages.getErrorIcon());
		}

		final List<OrderEntry> moduleEntries = new ArrayList<>();//遍历出module的列表
		final List<OrderEntry> awslibEntry = new ArrayList<>();//遍历出aws_lib的列表
		final List<OrderEntry> invalidEntry = new ArrayList<>();//无效的module
		modifiableModel.orderEntries().forEach(new Processor<OrderEntry>() {
			@Override
			public boolean process(OrderEntry orderEntry) {
				if (PluginConst.PLUGIN_AWS_LIBRARY_NAME.equals(orderEntry.getPresentableName())) {
					awslibEntry.add(orderEntry);
				} else {
					moduleEntries.add(orderEntry);
				}
				if (orderEntry instanceof ModuleOrderEntryImpl) {
					if (((ModuleOrderEntryImpl) orderEntry).getModule() == null) {
						invalidEntry.add(orderEntry);
					} else {
						// 不是java工程、排除的、web工程都移走
						Module tmp = ((ModuleOrderEntryImpl) orderEntry).getModule();
						if (!StdModuleTypes.JAVA.getId().equals(tmp.getModuleTypeName()) || PluginUtil.isExcludeModule(tmp.getName()) || PluginUtil.isAWSWebModule(tmp.getName())) {
							invalidEntry.add(orderEntry);
						}
					}
				}
				return true;
			}
		});
		//把module排序一下
		Collections.sort(moduleEntries, new Comparator<OrderEntry>() {
			@Override
			public int compare(OrderEntry o1, OrderEntry o2) {
				return o1.getPresentableName().compareTo(o2.getPresentableName());
			}
		});
		//aws_lib的保证放到最后
		moduleEntries.addAll(awslibEntry);
		modifiableModel.rearrangeOrderEntries(moduleEntries.toArray(new OrderEntry[0]));
		//移除无效的module
		if (invalidEntry.size() > 0) {
			for (OrderEntry orderEntry : invalidEntry) {
				modifiableModel.removeOrderEntry(orderEntry);
			}
		}
		if (module.getModuleFile() != null && module.getModuleFile().getParent() != null) {
			PluginUtil.updateModuleLibraries(project, module, new String[] { module.getModuleFile().getParent().getPath() + "/lib" });
		}

		if (awslibEntry.size() == 0) {//如果之前没遍历出来aws_lib，说明没有，加入到最后
			modifiableModel.addLibraryEntry(aws_lib);
		}
		if (modifiableModel.isChanged()) {
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				@Override
				public void run() {
					modifiableModel.commit();
				}
			});
			return module.getName();
		} else {
			return "";
		}
	}

	private String updateWebModuleDependencies(Module module) {
		Module releaseModule = PluginUtil.getReleaseModule(project, true);
		String webServerLib = releaseModule.getModuleFile().getParent().getPath() + "/webserver/lib";
		String portal = "";
		File webappsPath = new File(releaseModule.getModuleFile().getParent().getPath() + "/webserver/webapps");
		File[] files = webappsPath.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				File libJar = new File(file.getPath() + "/WEB-INF/lib/aws-infrastructure-web.jar");
				if (libJar.exists()) {
					portal = libJar.getParentFile().getPath();
					break;
				}
			}
		}
		PluginUtil.updateModuleLibraries(project, module, new String[] { webServerLib, portal });
		return module.getName();
	}

	private String updateUpgradeModuleDependencies(Module module) {
		String libPath = module.getModuleFile().getParent().getPath() + "/shell/lib";
		PluginUtil.updateModuleLibraries(project, module, new String[] { libPath });
		return module.getName();
	}
}
