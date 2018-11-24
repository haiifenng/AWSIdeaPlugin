package com.actionsoft.ideaplugins.module;

import com.actionsoft.ideaplugins.artifact.AWSArtifactRefresh;
import com.actionsoft.ideaplugins.link.LinkAppAction;
import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.impl.artifacts.ArtifactManagerImpl;

/**
 * 用于独立的apps中，创建module然后link到release目录中
 *
 * Created by Hayfeng on 2017.05.19.
 */
public class CreateModuleAndLinkAppAction extends LinkAppAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		VirtualFile[] data = DataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
		if (data != null) {
			StringBuilder message = new StringBuilder();
			Module releaseModule = PluginUtil.getReleaseModule(e.getProject(),true);
			if (releaseModule == null) {
				return;
			}
			CreateModules createModules = new CreateModules(e.getProject());
			AWSArtifactRefresh artifactRefresh = new AWSArtifactRefresh(e.getProject());
			ArtifactManager artifactManager = ArtifactManagerImpl.getInstance(e.getProject());
			final ModifiableArtifactModel modifiableArtifactModel = artifactManager.createModifiableModel();
			for (VirtualFile file : data) {
				String name = createModules.createModule(file);
				Module module = ModuleManager.getInstance(e.getProject()).findModuleByName(name);
				StringBuilder msg = new StringBuilder();
				artifactRefresh.createArtifact(releaseModule, modifiableArtifactModel, msg, module);
				if (name != null && !name.equals("")) {
					message.append(String.format("创建成功：%s", name)).append("\n");
				}
				// 创建软链接
				if (!checkFileExist(e, file)) {
					createLink(releaseModule, file);
				}
			}
		}
	}

	@Override
	public void update(AnActionEvent e) {
		String flag = "/apps/";
		Module appsModule = ModuleManager.getInstance(e.getProject()).findModuleByName("apps");
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
				e.getPresentation().setText("Create Modules And Link");
				for (VirtualFile virtualFile : data) {
					checkFile(e, flag, appsModule, virtualFile);
				}
				return;
			} else {
				checkFile(e, flag, appsModule, file);
			}
		}
	}

	private void checkFile(AnActionEvent e, String flag, Module appsModule, VirtualFile file) {
		String fileName = file.getName();
		String filePath = file.getPath();
		if (file.getName().startsWith("_bpm")) {
			e.getPresentation().setVisible(false);
			return;
		}
		if (filePath.contains(flag) && appsModule != null && !filePath.contains("release/")) {
			Module moduleByName = ModuleManager.getInstance(e.getProject()).findModuleByName(fileName);
			if (moduleByName != null) {
				e.getPresentation().setVisible(false);
				return;
			}
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
