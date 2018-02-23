package com.actionsoft.ideaplugins.library;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Hayfeng on 2017.01.07.
 */
public class FileSuffixFilter implements FilenameFilter {
	private final String suffix;

	public FileSuffixFilter(String suffix) {
		this.suffix = suffix.toLowerCase();
	}

	@Override
	public boolean accept(File dir, String name) {
		if (name.toLowerCase().endsWith(this.suffix)) {
			return true;
		}
		return false;
	}
}
