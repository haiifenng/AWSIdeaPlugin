package com.actionsoft.ideaplugins.file;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;

public class FileCopyAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		VirtualFile[] data = DataKeys.VIRTUAL_FILE_ARRAY.getData(anActionEvent.getDataContext());
		for (VirtualFile file : data) {
			FileCopy fc = new FileCopy();
			fc.copyToDesktop(file);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		VirtualFile file = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
		if (file == null) {
			e.getPresentation().setVisible(false);
		} else {
			if (file.isDirectory()) {
				e.getPresentation().setVisible(false);
				return;
			}
			e.getPresentation().setEnabledAndVisible(PluginUtil.isMacOSX());
		}
	}
}
