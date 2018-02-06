package com.actionsoft.ideaplugins.manifest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.actionsoft.ideaplugins.manifest.ui.SuspendAppConfigurable;
import com.actionsoft.ideaplugins.util.PluginUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;

/**
 * @author wangshibao
 *         Created on 2017-5-3
 */
public class SuspendAppAction {

	public void actionPerformed() {
		Module releaseModule = PluginUtil.getReleaseModule(SuspendAppConfigurable.currentProject, true);
		if (releaseModule == null) {
			return;
		}
		String installPath = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/";
		try {
			File installDir = new File(installPath);

			String[] appDirNames = installDir.list();
			Arrays.sort(appDirNames);
			String[] suspendedAppIds = PropertiesComponent.getInstance().getValues("suspendAppIds");
			List<String> suspendedAppIdList = new ArrayList<>();
			if (suspendedAppIds != null) {
				suspendedAppIdList = Arrays.asList(suspendedAppIds);
			}
			if (appDirNames != null) {
				for (int i = 0; i < appDirNames.length; i++) {
					String appId = appDirNames[i];
					try {
						SAXReader saxreader = new SAXReader();
						String xmlFile = installPath + appId + "/manifest.xml";
						Document doc = saxreader.read(xmlFile);
						Element root = doc.getRootElement();
						boolean isSuspend = false;
						for (Iterator iter = root.elementIterator(); iter.hasNext(); ) {
							Element element = (Element) iter.next();
							if (element != null) {
								if (element.getName().equals("suspend")) {
									String str = element.getTextTrim();
									if (str.equalsIgnoreCase("true")) {
										isSuspend = true;
										break;
									}
								}
							}
						}
						if (suspendedAppIdList.contains(appId) && isSuspend == false) {
							Map<String, String> values = new HashMap<>();
							values.put("suspend", String.valueOf(true));
							saveKeyValue(new File(xmlFile), values);
						} else if (!suspendedAppIdList.contains(appId) && isSuspend) {
							Map<String, String> values = new HashMap<>();
							values.put("suspend", String.valueOf(false));
							saveKeyValue(new File(xmlFile), values);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messages.showMessageDialog(SuspendAppConfigurable.currentProject, "禁止App自动启动发生错误", "错误", Messages.getErrorIcon());
			return;
		}
	}

	public final void saveKeyValue(File appConfigFile, Map<String, String> values) throws Exception {
		try {
			Document doc = getDocument(appConfigFile);
			for (String key : values.keySet()) {
				Element node = getElement(doc, key);
				node.setText(values.get(key));
			}
			saveXML(doc, appConfigFile);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 保存xml
	 *
	 * @param doc
	 * @param configFile
	 * @throws IOException
	 * @author Administrator
	 */
	public void saveXML(Document doc, File configFile) throws IOException {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileOutputStream(configFile), format);
		writer.write(doc);
		writer.close();
	}


	private final Document getDocument(File configFile) throws Exception {
		SAXReader saxreader = new SAXReader();
		Map map = new HashMap();
		map.put("app", "http://www.actionsoft.com.cn/app");
		saxreader.getDocumentFactory().setXPathNamespaceURIs(map);
		Document doc = saxreader.read(configFile);
		return doc;
	}

	/**
	 * 从document获取一个节点，如果没有自动添加。该方法只针对manifest.xml
	 *
	 * @param doc
	 * @param name
	 * @return
	 * @throws Exception
	 * @author Administrator
	 */
	private final Element getElement(Document doc, String name) throws Exception {
		Element node = null;
		String xpath = "app/app:" + name;
		java.util.List list = doc.selectNodes(xpath);
		if (list.size() > 0) {
			node = (Element) list.get(0);
		} else {
			node = doc.getRootElement().addElement(name);
		}
		return node;
	}
}