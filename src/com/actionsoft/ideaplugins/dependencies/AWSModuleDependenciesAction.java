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
		//		Collection<Module> modules = ModuleUtil.getModulesOfType(anActionEvent.getProject(), StdModuleTypes.JAVA);
		//		StringBuilder message = new StringBuilder();
		AWSModuleDependencies awsModuleDependencies = new AWSModuleDependencies(anActionEvent.getProject());
		String message = awsModuleDependencies.updateDependencies();
		PluginUtil.showNotification(anActionEvent, message);
		//		for (Module module : modules) {
		//			String msg = awsModuleDependencies.updateDependencies(module);
		//			if (!StringUtil.isEmpty(msg)) {
		//				message.append(msg).append("\n");
		//			}
		//		}
		//
		//		if (!StringUtil.isEmpty(message)) {
		//			message.setLength(message.toString().length() - 1);
		//			PluginUtil.showNotification(anActionEvent,String.format("更新了以下Module：\n%s", message.toString()));
		//		} else {
		//			PluginUtil.showNotification(anActionEvent,"没有Module被更新");
		//		}
	}

}
