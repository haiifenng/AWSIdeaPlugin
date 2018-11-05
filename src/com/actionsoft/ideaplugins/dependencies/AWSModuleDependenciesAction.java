package com.actionsoft.ideaplugins.dependencies;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Hayfeng on 2017.05.07.
 */
public class AWSModuleDependenciesAction extends AnAction {

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		AWSModuleDependencies awsModuleDependencies = new AWSModuleDependencies(anActionEvent.getProject());
		String message = awsModuleDependencies.updateDependencies();
		PluginUtil.showNotification(anActionEvent, message);
	}

}
