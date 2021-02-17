package com.actionsoft.ideaplugins.module;

import com.actionsoft.ideaplugins.helper.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 用于在release目录中的app文件夹创建module
 * Created by Haiifenng on 2017.05.19.
 */
public class CreateModulesAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		VirtualFile[] data = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
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
				PluginUtil.showNotification(e, message.toString());
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		VirtualFile[] data = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
		if (file == null) {
			e.getPresentation().setVisible(false);
		} else {
			if (!file.isDirectory()) {
				e.getPresentation().setVisible(false);
				return;
			}
			if (data.length > 1) {
				for (VirtualFile virtualFile : data) {
					checkFile(e, virtualFile, true);
				}
				e.getPresentation().setText("Create Modules");
				return;
			} else {
				checkFile(e, file, false);
			}
		}
	}

	private void checkFile(AnActionEvent e, VirtualFile file, boolean isMulti) {
		String flag = "/apps/install/";
		String flagAWS = "/aws/modules/";
		String flagSec = "/aws-security/modules/";
		String filePath = file.getPath();
		if (file.getName().startsWith("_bpm")) {
			e.getPresentation().setVisible(false);
			return;
		}
		if (PluginUtil.checkManifestXml(file)) {
			checkName(e, "", isMulti, file);
		} else if (filePath.contains(flag)) {
			checkName(e, flag, isMulti, file);
		} else if (filePath.contains(flagAWS)) {
			checkName(e, flagAWS, isMulti, file);
		} else if (filePath.contains(flagSec)) {
			checkName(e, flagSec, isMulti, file);
		} else {
			e.getPresentation().setVisible(false);
		}
	}

	private void checkName(AnActionEvent e, String flag, boolean isMulti, VirtualFile file) {
		String fileName = file.getName();
		String filePath = file.getPath();
		try {
			Module moduleByName = ModuleManager.getInstance(e.getProject()).findModuleByName(fileName);
			if (moduleByName != null) {
				e.getPresentation().setVisible(false);
				return;
			}
		} catch (java.lang.Exception exception) {
			exception.printStackTrace();
		}

		if (StringUtil.isNotEmpty(flag)) {
			String moduleId = filePath.substring(filePath.indexOf(flag) + flag.length());
			if (moduleId.contains("/")) {
				//说明是子文件夹或文件
				e.getPresentation().setVisible(false);
				return;
			}
		}
		e.getPresentation().setText(isMulti ? "Create Modules" : getButtonName(fileName));
	}

	private String getButtonName(String test) {
		test = "'" + test + "'";
		StringBuilder sb = new StringBuilder(40);
		sb.append("Create Module").append(" ");
		int length = test.length();
		if (length > 23) {
			if (StringUtil.startsWithChar(test, '\'')) {
				sb.append("\'");
			}
			sb.append("...");
			sb.append(test.substring(length - 20, length));
		} else {
			sb.append(test);
		}
		return sb.toString();
	}

}
