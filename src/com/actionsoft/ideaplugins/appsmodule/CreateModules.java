package com.actionsoft.ideaplugins.appsmodule;

import java.io.File;
import java.util.Collection;

import com.actionsoft.ideaplugins.dependencies.AWSModuleDependencies;
import com.actionsoft.ideaplugins.libraray.AWSLibraryRefresh;
import com.actionsoft.ideaplugins.util.PluginUtil;
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
 * Created by Hayfeng on 2017.05.19.
 */
public class CreateModules {

	private Project project;

	public CreateModules(Project project) {
		this.project = project;
	}

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

		ContentEntry contentEntry = modifiableModule.addContentEntry(file);
		VirtualFile srcPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(file.getPath(), "src"));
		if (srcPath != null) {
			contentEntry.addSourceFolder(srcPath, false);
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
		StringBuilder message = new StringBuilder();
		AWSModuleDependencies awsModuleDependencies = new AWSModuleDependencies(project);
		for (Module m : modules) {
			awsModuleDependencies.updateDependencies(m);
		}

		return moduleName;
	}

}
