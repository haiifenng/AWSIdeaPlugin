package com.actionsoft.ideaplugins.libraray;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Hayfeng on 2017.01.07.
 */
public class AWSLibraryRefreshAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		AWSLibraryRefresh r = new AWSLibraryRefresh(anActionEvent.getProject());
		r.refreshAWSLibrary();
		PluginUtil.showNotification(anActionEvent,"AWS Libraries 更新完毕");
	}
}
