package com.actionsoft.ideaplugins.dependencies;

import java.util.Collection;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

/**
 * Created by Hayfeng on 2017.05.07.
 */
public class AWSModuleDependenciesAction extends AnAction {
	private Project project;

	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		this.project = anActionEvent.getProject();
		Collection<Module> modules = ModuleUtil.getModulesOfType(anActionEvent.getProject(), StdModuleTypes.JAVA);
		StringBuilder message = new StringBuilder();
		AWSModuleDependencies awsModuleDependencies = new AWSModuleDependencies(project);
		for (Module module : modules) {
			String msg = awsModuleDependencies.updateDependencies(module);
			if (!StringUtil.isEmpty(msg)) {
				message.append(msg).append("\n");
			}
		}

		if (!StringUtil.isEmpty(message)) {
			message.setLength(message.toString().length() - 1);
			PluginUtil.showNotification(anActionEvent,String.format("更新了以下Module：\n%s", message.toString()));
//			Messages.showMessageDialog(project, String.format("更新了以下Module：\n%s", message.toString()), "提示", Messages.getInformationIcon());
		} else {
			PluginUtil.showNotification(anActionEvent,"没有Module被更新");
//			Messages.showInfoMessage("没有Module被更新", "提示");
		}
	}

}
