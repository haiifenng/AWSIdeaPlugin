/*
 * Copyright(C)2001-2018 Actionsoft Co.,Ltd
 * AWS(Actionsoft workflow suite) BPM(Business Process Management) PLATFORM Source code
 * AWS is a application middleware for BPM System


 * 本软件工程编译的二进制文件及源码版权归北京炎黄盈动科技发展有限责任公司所有，
 * 受中国国家版权局备案及相关法律保护，未经书面法律许可，任何个人或组织都不得泄漏、
 * 传播此源码文件的全部或部分文件，不得对编译文件进行逆向工程，违者必究。

 * $$本源码是炎黄盈动最高保密级别的文件$$
 *
 * http://www.actionsoft.com.cn
 *
 */
package com.actionsoft.ideaplugins.tools;

import com.actionsoft.ideaplugins.dependencies.AWSModuleDependenciesAction;
import com.actionsoft.ideaplugins.library.AWSLibraryRefreshAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Description:
 * <p>
 * Date: 2018/3/28 21:59
 *
 * @author zhanghf
 */
public class AWSLibraryAndDependRefreshAction extends AnAction {
	@Override
	public void actionPerformed(AnActionEvent anActionEvent) {
		AWSLibraryRefreshAction lib = new AWSLibraryRefreshAction();
		lib.actionPerformed(anActionEvent);

		AWSModuleDependenciesAction depend = new AWSModuleDependenciesAction();
		depend.actionPerformed(anActionEvent);
	}
}
