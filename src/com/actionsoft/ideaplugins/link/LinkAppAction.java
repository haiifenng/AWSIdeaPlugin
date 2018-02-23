package com.actionsoft.ideaplugins.link;

import java.io.File;
import java.io.IOException;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by Hayfeng on 2017.05.19.
 */
public class LinkAppAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		VirtualFile[] data = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		if (data != null) {
			StringBuilder message = new StringBuilder();
			Module releaseModule = PluginUtil.getReleaseModule(e.getProject(), true);
			if (releaseModule == null) {
				return;
			}
			for (VirtualFile file : data) {
				createLink(releaseModule, file);
			}
		}
	}

	protected void createLink(Module releaseModule, VirtualFile file) {
		String targetFile = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/" + file.getName();
		String sourceFile = file.getPath();
		String cmd = "";
		if (PluginUtil.isMacOSX()) {
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
		String flag = "/apps/";
		Module appsModule = ModuleManager.getInstance(e.getProject()).findModuleByName("apps");
		Module releaseModule = PluginUtil.getReleaseModule(e.getProject(), false);
		if (releaseModule == null) {
			e.getPresentation().setVisible(false);
			return;
		}
		VirtualFile[] data = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
		if (file == null) {
			e.getPresentation().setVisible(false);
		} else {
			if (!file.isDirectory()) {
				e.getPresentation().setVisible(false);
				return;
			}
			if (data.length > 1) {
				e.getPresentation().setVisible(false);
				return;
			}
			if (file.getPath().contains("/apps/install/")) {
				e.getPresentation().setVisible(false);
				return;
			}
			String targetFilePath = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/" + file.getName();
			File f = new File(targetFilePath);
			if (f.exists()) {
				e.getPresentation().setText("Already Linked");
				e.getPresentation().setEnabled(false);
				return;
			}
			String filePath = file.getPath();
			if (file.getName().startsWith("_bpm")) {
				e.getPresentation().setVisible(false);
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
	}

}
