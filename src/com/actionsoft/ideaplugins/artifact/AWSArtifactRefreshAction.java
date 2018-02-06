package com.actionsoft.ideaplugins.artifact;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;

/**
 * Created by Hayfeng on 2017.01.16.
 */
public class AWSArtifactRefreshAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		Module releaseModule = PluginUtil.getReleaseModule(anActionEvent.getProject(),true);
		if (releaseModule == null) {
			return;
		}
		AWSArtifactRefresh a = new AWSArtifactRefresh(anActionEvent.getProject());
		String msg = a.refreshArtifact();
		String message = "";
		if (msg.length() == 0) {
			message = "没有创建新的Artifact";
		} else {
			msg.substring(msg.length()-1);
			message = String.format("创建了以下新的Artifacts：\n%s", msg.toString());
		}
		PluginUtil.showNotification(anActionEvent,message);
	}
}
