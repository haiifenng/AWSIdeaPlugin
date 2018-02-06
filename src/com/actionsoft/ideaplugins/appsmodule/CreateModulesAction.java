package com.actionsoft.ideaplugins.appsmodule;

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
public class CreateModulesAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		VirtualFile[] data = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		if (data != null) {
			StringBuilder message = new StringBuilder();
			CreateModules createModules = new CreateModules(e.getProject());
			for (VirtualFile file : data) {
				String name = createModules.createModule(file);
				if (name != null && !name.equals("")) {
					message.append(String.format("创建成功：%s", name)).append("\n");
				}
			}
			if (message.length() > 0) {
				message.append("同时更新了AWS Libraries以及Module的依赖库");
//				Messages.showInfoMessage(message.toString(), "提示");
				PluginUtil.showNotification(e,message.toString());
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		String flag = "/apps/install/";
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
				for (VirtualFile virtualFile : data) {
					if (virtualFile.getName().startsWith("_bpm") || !virtualFile.getPath().contains(flag)) {
						e.getPresentation().setVisible(false);
						return;
					}
				}
				e.getPresentation().setText("Create Modules");
				return;
			}
			String fileName = file.getName();
			String filePath = file.getPath();
			if (file.getName().startsWith("_bpm")) {
				e.getPresentation().setVisible(false);
				return;
			}
			if (filePath.contains(flag)) {
				String appId = filePath.substring(filePath.indexOf(flag) + flag.length());
				if (appId.contains("/")) {//说明是子文件夹或文件
					e.getPresentation().setVisible(false);
				}
				if (e.getPresentation().getText().equals("Create AWS App Module")) {
					Module moduleByName = ModuleManager.getInstance(e.getProject()).findModuleByName(appId);
					if (moduleByName != null) {
						e.getPresentation().setVisible(false);
					} else {
						e.getPresentation().setText(String.format("Create Module '%s'", fileName));
						e.getPresentation().setEnabledAndVisible(true);
					}
				}
			} else {
				e.getPresentation().setVisible(false);
			}
		}
	}

}
