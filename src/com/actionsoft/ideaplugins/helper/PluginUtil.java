package com.actionsoft.ideaplugins.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.actionsoft.ideaplugins.library.FileSuffixFilter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Processor;

/**
 * Created by Haiifenng on 2017.01.16.
 */
public class PluginUtil {
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isLinux() {
		return OS.indexOf("linux") >= 0;
	}

	public static boolean isMacOSX() {
		return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
	}

	public static boolean isWindows() {
		return OS.indexOf("windows") >= 0;
	}

	@Nullable
	public static Module getReleaseModule(Project project, boolean isMsg) {
		Module releaseModule = ModuleManager.getInstance(project).findModuleByName("release");
		if (releaseModule == null) {
			if (isMsg) {
				Messages.showMessageDialog(project, "当前Project中没有命名为[release]的Module", "提示", Messages.getErrorIcon());
			}
			return null;
		}
		//校验是不是一个有效的release
		String releasePath = releaseModule.getModuleFile().getParent().getPath();
		File file_release6_1 = new File(releasePath + "/bin/conf/server.xml");
		File file_release6_2 = new File(releasePath + "/bin/lib/aws-license.jar");

		File file_release5_1 = new File(releasePath + "/bin/system.xml");
		File file_release5_2 = new File(releasePath + "/bin/lib/aws.platform.jar");
		if (file_release6_1.exists() && file_release6_2.exists()) {//AWS6版本
			return releaseModule;
		} else if (file_release5_1.exists() && file_release5_2.exists()) {//AWS5版本
			return releaseModule;
		} else {
			if (isMsg) {
				Messages.showMessageDialog(project, "当前Project中的[release]的不是一个有效的AWS资源", "提示", Messages.getErrorIcon());
			}
			return null;
		}
	}

	public static void showNotification(AnActionEvent anActionEvent, String content) {
		Notification notification = new Notification("AWS Developer Plugin", "AWS Developer Plugin", content, NotificationType.INFORMATION);
		notification.notify(AnAction.getEventProject(anActionEvent));
	}

	public static void showNotification(Project project, String content) {
		Notification notification = new Notification("AWS Developer Plugin", "AWS Developer Plugin", content, NotificationType.INFORMATION);
		notification.notify(project);
	}

	public static List<File> findAllFileInPath(String rootPath, FilenameFilter filenameFilter) {
		List<File> result = new ArrayList<>();
		File rootFile = new File(rootPath);
		LinkedList<File> list = new LinkedList<>();
		File[] childs = rootFile.listFiles();
		if (childs != null) {
			for (File child : childs) {
				list.add(child);
			}

		}
		while (!list.isEmpty()) {
			File wrap = list.removeFirst();
			if ((!wrap.isDirectory()) && (filenameFilter.accept(wrap, wrap.getName()))) {
				result.add(wrap);
			}

			childs = wrap.listFiles();
			if (childs != null) {
				for (File child : childs) {
					list.add(child);
				}
			}
		}
		return result;
	}

	public static boolean updateModuleLibraries(Project project, Module module, String[] libPaths) {
		String libName = "lib:" + module.getName() + "";
		List<VirtualFile> jarList = new ArrayList<>();
		for (String libPath : libPaths) {
			File fLibPath = new File(libPath);
			List<File> allFileInPath = PluginUtil.findAllFileInPath(fLibPath.getPath(), new FileSuffixFilter(".jar"));
			for (File file : allFileInPath) {
				if (file.getName().contains(module.getName())) {
					continue;
				}
				VirtualFile jar = JarFileSystem.getInstance().findFileByPath(file.getPath() + "!/");
				if (jar != null) {
					jarList.add(jar);
				}
			}
		}
		if (jarList.isEmpty()) {
			return false;
		}
		LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
		Library _lib = libraryTable.getLibraryByName(libName);
		final LibraryTable.ModifiableModel projectModel = libraryTable.getModifiableModel();
		if (_lib != null) {
			projectModel.removeLibrary(_lib);
		}
		_lib = projectModel.createLibrary(libName);

		final Library.ModifiableModel newLibModel = _lib.getModifiableModel();
		for (VirtualFile virtualFile : jarList) {
			newLibModel.addRoot(virtualFile, OrderRootType.CLASSES);
		}

		final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
		final List<OrderEntry> libEntry = new ArrayList<>();//遍历出lib的列表
		OrderEnumerator orderEnumerator = modifiableModel.orderEntries();
		orderEnumerator.forEach(new Processor<OrderEntry>() {
			@Override
			public boolean process(OrderEntry orderEntry) {
				if (orderEntry.getPresentableName().equals(libName)) {
					libEntry.add(orderEntry);
				}
				return true;
			}
		});
		for (OrderEntry orderEntry : libEntry) {
			modifiableModel.removeOrderEntry(orderEntry);
		}
		modifiableModel.addLibraryEntry(_lib);

		ApplicationManager.getApplication().runWriteAction(new Runnable() {
			@Override
			public void run() {
				newLibModel.commit();
				projectModel.commit();
				modifiableModel.commit();
			}
		});
		return true;
	}

	public static boolean isAvailableAWSModule(Module module) {
		if (module == null) {
			return false;
		}
		String name = module.getName();
		if (PluginUtil.isExcludeModule(name)) {
			return false;
		}
		if (name.startsWith("aws-")) {
			return true;
		} else {
			String moduleFilePath = module.getModuleFilePath();
			if (moduleFilePath.contains("apps/install") || moduleFilePath.contains("apps/")) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isExcludeModule(String name) {
		String[] excludes = { "doc", "release", "aws-all", "aws", "apps", "web", "h5designer", "aws-security", "security", "aws-schema" };
		List<String> strings = Arrays.asList(excludes);
		return strings.contains(name);
	}

	public static boolean isExcludeModule(Module module) {
		VirtualFile file = module.getModuleFile();
		if (file == null) {
			return false;
		}
		VirtualFile srcMainPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(file.getParent().getPath(), "src/main/java"));
		VirtualFile srcPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(file.getParent().getPath(), "src"));
		if (srcMainPath != null && srcMainPath.exists()) {
			return false;
		}
		if (srcPath != null && srcPath.exists()) {
			return false;
		}
		return true;
	}

	public static boolean isAWSWebModule(String name) {
		String[] excludes = { "aws-infrastructure-web", "aws-node-wrapper", "aws-coe-web", "aws-api-client" };
		List<String> strings = Arrays.asList(excludes);
		return strings.contains(name);
	}

	public static List<String> getAppDirs(File installDir) {
		List<String> list = new ArrayList<>();
		File[] files = installDir.listFiles();
		for (File file : files) {
			if (file.getName().equals(".DS_Store")) {
				continue;
			}
			if (file.isDirectory() && !file.getName().startsWith("_bpm")) {
				list.add(file.getName());
			}
		}
		return list;
	}

	public static boolean checkManifestXml(VirtualFile file) {
		File manifestFile = new File(file.getPath() + "/manifest.xml");
		return manifestFile.exists();
	}

}
