package com.actionsoft.ideaplugins.link;

import java.io.File;
import java.io.IOException;

import com.actionsoft.ideaplugins.helper.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by Haiifenng on 2017.05.19.
 */
public class LinkAppAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		VirtualFile[] data = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		if (data != null) {
			StringBuilder message = new StringBuilder();
			Module releaseModule = PluginUtil.getReleaseModule(e.getProject(), true);
			if (releaseModule == null) {
				return;
			}
			for (VirtualFile file : data) {
				if (checkFileExist(e, file)) {
					continue;
				}
				createLink(releaseModule, file);
			}
			String installPath = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/";
			VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(installPath));
			virtualFile.refresh(false, true);
			PluginUtil.showNotification(e, "符号/目录链接创建成功");
		}
	}

	protected void createLink(Module releaseModule, VirtualFile file) {
		String targetFile = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/" + file.getName();
		String sourceFile = file.getPath();
		String cmd = "";
		if (PluginUtil.isMacOSX() || PluginUtil.isLinux()) {
			cmd = "ln -s " + sourceFile + " " + targetFile;
			link(cmd);
		} else if (PluginUtil.isWindows()) {
			sourceFile = sourceFile.replaceAll("/", "\\\\");
			targetFile = targetFile.replaceAll("/", "\\\\");
			cmd = "cmd.exe /c mklink /j " + targetFile + " " + sourceFile;
			link(cmd);
		}
	}

	protected void link(String cmd) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			process.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(AnActionEvent e) {
		VirtualFile[] data = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
		if (file == null) {
			e.getPresentation().setVisible(false);
		} else {
			if (data.length > 1) {
				for (VirtualFile virtualFile : data) {
					checkFile(e, virtualFile, true);
				}
				e.getPresentation().setText("Link Apps");
			} else {
				checkFile(e, file, false);
			}
		}
	}

	private void checkFile(AnActionEvent e, VirtualFile file, boolean isMulti) {
		Module appsModule = ModuleManager.getInstance(e.getProject()).findModuleByName("apps");
		String flag = "/apps/";
		if (PluginUtil.getReleaseModule(e.getProject(), false) == null) {
			e.getPresentation().setVisible(false);
			return;
		}
		if (!file.isDirectory()) {
			e.getPresentation().setVisible(false);
			return;
		}
		if (file.getPath().contains("/apps/install/") || file.getPath().contains("release/")) {
			e.getPresentation().setVisible(false);
			return;
		}
		if (!isMulti && checkFileExist(e, file)) {
			e.getPresentation().setText("Already Linked");
			e.getPresentation().setEnabled(false);
			return;
		}
		String filePath = file.getPath();
		if (file.getName().startsWith("_bpm")) {
			e.getPresentation().setVisible(false);
			return;
		}
		if (checkManifestXml(e, file)) {
			return;
		}
		if (filePath.contains(flag) && appsModule != null) {
			String appId = filePath.substring(filePath.indexOf(flag) + flag.length());
			//说明是子文件夹或文件
			if (appId.contains("/")) {
				e.getPresentation().setVisible(false);
			}
		} else {
			e.getPresentation().setVisible(false);
		}
	}

	protected boolean checkManifestXml(AnActionEvent e, VirtualFile file) {
		File manifestFile = new File(file.getPath()+"/manifest.xml");
		if (!manifestFile.exists()) {
			e.getPresentation().setVisible(false);
			return true;
		}
		return false;
	}

	protected boolean checkFileExist(AnActionEvent e, VirtualFile file) {
		Module releaseModule = PluginUtil.getReleaseModule(e.getProject(), false);
		String targetFilePath = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/" + file.getName();
		return new File(targetFilePath).exists();
	}

}
