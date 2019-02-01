package com.actionsoft.ideaplugins.module;

import java.io.File;
import java.util.Collection;

import com.actionsoft.ideaplugins.dependencies.AWSModuleDependencies;
import com.actionsoft.ideaplugins.helper.PluginUtil;
import com.actionsoft.ideaplugins.library.AWSLibraryRefresh;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by Haiifenng on 2017.05.19.
 */
public class CreateModules {

	private Project project;

	public CreateModules(Project project) {
		this.project = project;
	}

	/**
	 * 根据右键选择file创建module
	 *
	 * @param file
	 * @return
	 */
	public String createModule(VirtualFile file) {
		if (file == null) {
			return "";
		}
		String moduleName = file.getName();
		final ModifiableModuleModel moduleManager = ModuleManager.getInstance(project).getModifiableModel();

		String ideaModuleFile = file.getPath() + File.separator + moduleName + ".iml";
		Module module = moduleManager.newModule(ideaModuleFile, StdModuleTypes.JAVA.getId());

		final ModifiableRootModel modifiableModule = ModuleRootManager.getInstance(module).getModifiableModel();
		modifiableModule.setSdk(ProjectRootManager.getInstance(project).getProjectSdk());
		modifiableModule.inheritSdk();

		ContentEntry contentEntry = modifiableModule.addContentEntry(file);

		VirtualFile srcMainPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(file.getPath(), "src/main/java"));
		VirtualFile srcTestPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(file.getPath(), "src/test/java"));
		VirtualFile srcPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(file.getPath(), "src"));
		if (srcMainPath != null && srcMainPath.exists()) {
			contentEntry.addSourceFolder(srcMainPath, false);
		} else {
			if (srcPath != null && srcPath.exists()) {
				contentEntry.addSourceFolder(srcPath, false);
			}
		}
		if (srcTestPath != null && srcTestPath.exists()) {
			contentEntry.addSourceFolder(srcTestPath, true);
		}
		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				modifiableModule.commit();
				moduleManager.commit();

			}
		});

		PluginUtil.updateModuleLibraries(project, module, new String[] { file.getPath() + "/lib" });
		//更新aws_lib库
		AWSLibraryRefresh r = new AWSLibraryRefresh(project);
		r.refreshAWSLibrary();
		//更新所有Module的依赖
		Collection<Module> modules = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);
		AWSModuleDependencies awsModuleDependencies = new AWSModuleDependencies(project);
		for (Module m : modules) {
			awsModuleDependencies.updateDependencies(m);
		}
		return moduleName;
	}

}
