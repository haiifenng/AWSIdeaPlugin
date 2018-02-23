package com.actionsoft.ideaplugins.file;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * 文件复制相关操作
 *
 * @author Haiifenng
 */
public class FileCopy {
	private String fileSeparator = System.getProperty("file.separator");

	public void copyToDesktop(VirtualFile file) {
		String userHome = System.getProperty("user.home");
		String target = userHome + fileSeparator + "Desktop";
		Path targetPath = Paths.get(target);
		boolean exists = Files.exists(targetPath, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
		Path sourcePath = Paths.get(file.getPath());
		Path destinationPath = Paths.get(target + fileSeparator + file.getName());
		if (exists) {
			try {
				Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (FileAlreadyExistsException e) {
			} catch (IOException e) {
			}
		}
	}
}
