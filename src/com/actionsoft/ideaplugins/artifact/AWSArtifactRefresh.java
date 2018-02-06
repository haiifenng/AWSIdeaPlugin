package com.actionsoft.ideaplugins.artifact;

import java.util.Collection;

import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.artifacts.ArtifactManager;
import com.intellij.packaging.artifacts.ModifiableArtifact;
import com.intellij.packaging.artifacts.ModifiableArtifactModel;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.artifacts.ArtifactManagerImpl;
import com.intellij.packaging.impl.artifacts.JarArtifactType;
import com.intellij.packaging.impl.elements.ArchivePackagingElement;

/**
 * Created by Hayfeng on 2017.01.16.
 */
public class AWSArtifactRefresh {
	private Project project;

	public AWSArtifactRefresh(Project project) {
		this.project = project;
	}

	/**
	 * 返回创建的Artifact，多个用回车符分隔
	 *
	 * @return
	 */
	public String refreshArtifact() {
		Module releaseModule = PluginUtil.getReleaseModule(project, true);
		if (releaseModule == null) {
			return "";
		}
		Collection<Module> modules = ModuleUtil.getModulesOfType(this.project, StdModuleTypes.JAVA);
		ArtifactManager artifactManager = ArtifactManagerImpl.getInstance(project);
		final ModifiableArtifactModel modifiableArtifactModel = artifactManager.createModifiableModel();
		StringBuilder msg = new StringBuilder();
		for (Module module : modules) {
			createArtifact(releaseModule, modifiableArtifactModel, msg, module);
		}
		return msg.toString();
	}

	public void createArtifact(Module releaseModule, ModifiableArtifactModel modifiableArtifactModel, StringBuilder msg, Module module) {
		if (!PluginUtil.isAvailableAWSModule(module)) {
			return;
		}
		if (module.getModuleFile() == null) {
			return;
		}
		if (module.getName().equals("aws-schema")) {
			return;
		}
		//判断jar配置是否存在
		String name = module.getName();
		String jarFileName = name;
		if (name.equals("aws-sdk")) {
			name = "aws-sdk-local (include source)";
			jarFileName = "aws-sdk-local";
		}
		//aws开头的module的输出路径
		String outputPath = releaseModule.getModuleFile().getParent().getPath() + "/bin/lib";
		//app的module输出路径
		if (module.getModuleFilePath().contains("apps/install/")) {
			outputPath = module.getModuleFile().getParent().getPath() + "/lib";
		} else if (module.getModuleFilePath().contains("apps/") && !module.getModuleFilePath().contains("install/")) {
			outputPath = module.getModuleFile().getParent().getPath() + "/lib";
		} else if (module.getName().equals("aws-platform-upgrade")) {
			//aws-platform-upgrade的module输出路径
			outputPath = module.getModuleFile().getParent().getPath() + "/shell/lib";
		} else if (name.equals("aws-infrastructure-web")) {
			//web的Module输出路径
			outputPath = releaseModule.getModuleFile().getParent().getPath() + "/webserver/webapps/portal/WEB-INF/lib";
		}

		ArchivePackagingElement archivePackagingElement = new ArchivePackagingElement(jarFileName + ".jar");
		final PackagingElement<?> moduleOutput = PackagingElementFactory.getInstance().createModuleOutput(module);
		archivePackagingElement.addFirstChild(moduleOutput);
		//sdk的时候，把源码文件夹添加进去
		if (module.getName().equals("aws-sdk")) {
			final PackagingElement<?> srcOutput = PackagingElementFactory.getInstance().createDirectoryCopyWithParentDirectories(module.getModuleFile().getParent().getPath() + "/src", "");
			archivePackagingElement.addOrFindChild(srcOutput);
		}
		Artifact tmpArtifact = findArtifact(name);
		if (tmpArtifact == null) {
			final ModifiableArtifact artifact = modifiableArtifactModel.addArtifact(name, JarArtifactType.getInstance(), archivePackagingElement);
			artifact.setOutputPath(outputPath);
			artifact.setBuildOnMake(false);
			msg.append(name).append("\n");
			ApplicationManager.getApplication().runWriteAction(new Runnable() {
				@Override
				public void run() {
					modifiableArtifactModel.commit();
				}
			});
		}
	}

	private Artifact findArtifact(String artifactName) {
		ArtifactManager artifactManager = ArtifactManagerImpl.getInstance(project);
		Artifact[] artifacts = artifactManager.getSortedArtifacts();
		for (Artifact artifact : artifacts) {
			if (artifact.getName().equals(artifactName)) {
				return artifact;
			}
		}
		return null;
	}

}
