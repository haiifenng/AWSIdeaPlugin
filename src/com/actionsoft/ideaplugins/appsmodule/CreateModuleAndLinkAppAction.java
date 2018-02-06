package com.actionsoft.ideaplugins.appsmodule;

import java.io.File;

import com.actionsoft.ideaplugins.artifact.AWSArtifactRefresh;
import com.actionsoft.ideaplugins.link.LinkAppAction;
import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.impl.artifacts.ArtifactManagerImpl;

/**
 * Created by Hayfeng on 2017.05.19.
 */
public class CreateModuleAndLinkAppAction extends LinkAppAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
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
				String targetFilePath = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/" + file.getName();
				File f = new File(targetFilePath);
				if (!f.exists()) {
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
		String text = e.getPresentation().getText();
		if (file == null) {
			e.getPresentation().setVisible(false);
		} else {
			if (!file.isDirectory()) {
				e.getPresentation().setVisible(false);
				return;
			}
			if (data.length > 1) {
				for (VirtualFile virtualFile : data) {
					if (virtualFile.getName().startsWith("_bpm")) {
						e.getPresentation().setVisible(false);
						return;
					}
				}
				e.getPresentation().setText("Create Modules And Link");
				return;
			}
			String fileName = file.getName();
			String filePath = file.getPath();
			if (file.getName().startsWith("_bpm")) {
				e.getPresentation().setVisible(false);
				return;
			}
			if (filePath.contains(flag) && appsModule != null) {
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

}
