package com.actionsoft.ideaplugins.libraray;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by Hayfeng on 2017.01.07.
 */
public class AWSLibraryRefresh {
	private LibraryTable projectTable;
	private Project project;

	public AWSLibraryRefresh(Project project) {
		this.project = project;
		this.projectTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
	}

	public void refreshAWSLibrary() {
		Module releaseModule = PluginUtil.getReleaseModule(project,true);
		if (releaseModule == null) {
			return;
		}
		Library aws_lib = projectTable.getLibraryByName("aws_lib");
		final LibraryTable.ModifiableModel projectModel = this.projectTable.getModifiableModel();
		if (aws_lib != null) {
			projectModel.removeLibrary(aws_lib);
		}
		aws_lib = projectModel.createLibrary("aws_lib");

		final Library.ModifiableModel newLibModel = aws_lib.getModifiableModel();

		VirtualFile releaseVFile = releaseModule.getModuleFile();
		List<File> awsLibJarFiles = findAWSLibJarFiles(releaseVFile);

		Collection<Module> modules = ModuleUtil.getModulesOfType(this.project, StdModuleTypes.JAVA);
		List<String> jarName = new ArrayList<>();
		for (Module module : modules) {
			String jar = module.getName() + ".jar";
			if (module.getName().equals("aws-sdk")) {
				jar = "aws-sdk-local.jar";
			}
			jarName.add(jar);
		}
		for (File file : awsLibJarFiles) {
			if (jarName.contains(file.getName())) {
				continue;
			}
			VirtualFile jar = JarFileSystem.getInstance().findFileByPath(file.getPath() + "!/");
			if (jar != null) {
				newLibModel.addRoot(jar, OrderRootType.CLASSES);
			}
		}
		//处理源码
		for (Module module : modules) {
			if (PluginUtil.isExcludeModule(module.getName())) {
				continue;
			}
			if(PluginUtil.isAWSWebModule(module.getName())){
				continue;
			}
			if(module.getName().equals("aws-platform-upgrade")){
				continue;
			}
			VirtualFile moduleFile = module.getModuleFile();
			if (moduleFile != null) {
				final ModifiableRootModel modifiableModule = ModuleRootManager.getInstance(module).getModifiableModel();
				VirtualFile[] sourceRoots = modifiableModule.getSourceRoots();
				for (VirtualFile sourceRoot : sourceRoots) {
					if (sourceRoot.getPath().contains("/test/")) {
						continue;
					}
					newLibModel.addRoot(sourceRoot, OrderRootType.SOURCES);
				}
//				File src = new File(moduleFile.getParent().getPath() + "/java");
//				if (!src.exists()) {
//					src = new File(moduleFile.getParent().getPath() + "/src/main/java");
//				}
//				if (!src.exists()) {
//					src = new File(moduleFile.getParent().getPath() + "/src");
//				}
//				String srcUrl = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, src.getPath());
//				VirtualFile vFile = VirtualFileManager.getInstance().findFileByUrl(srcUrl);
//				if (vFile != null) {
//					newLibModel.addRoot(vFile, OrderRootType.SOURCES);
//				}
			}
		}

		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				newLibModel.commit();
				projectModel.commit();
			}
		});
	}

	private List<File> findAWSLibJarFiles(VirtualFile releaseFile) {
		String releaseIml = releaseFile.getPath();
		File f = new File(releaseIml);
		File binlib = new File(f.getParent() + "/bin/lib");
		File binjdbc = new File(f.getParent() + "/bin/jdbc");

		List<File> list = null;
		try {
			List<File> files = PluginUtil.findAllFileInPath(binlib.getCanonicalPath(), new FileSuffixFilter(".jar"));
			list = new ArrayList<File>();
			for (int i = 0; i < files.size(); i++) {
				list.add(files.get(i));
			}
			files = PluginUtil.findAllFileInPath(binjdbc.getCanonicalPath(), new FileSuffixFilter(".jar"));
			for (int i = 0; i < files.size(); i++) {
				list.add(files.get(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}


}
