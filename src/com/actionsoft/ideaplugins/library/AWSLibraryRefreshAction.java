package com.actionsoft.ideaplugins.library;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by Haiifenng on 2017.01.07.
 */
public class AWSLibraryRefreshAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		System.out.println("dddd");
		AWSLibraryRefresh r = new AWSLibraryRefresh(anActionEvent.getProject());
		r.refreshAWSLibrary();
	}
}
