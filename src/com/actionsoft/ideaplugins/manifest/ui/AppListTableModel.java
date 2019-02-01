package com.actionsoft.ideaplugins.manifest.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.actionsoft.ideaplugins.helper.PluginUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;

/**
 * @author wangshibao
 * Created on 2017-5-17
 */
public class AppListTableModel extends AbstractTableModel {

	public final String[] columnNames = { "应用ID", "应用名称", "当前状态", "设定状态" };
	public Object[][] values = {};
	boolean isValuesInited = false;

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 3) {
			return true;
		}
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 3) {
			return Boolean.class;
		}
		return String.class;
	}

	public void initValues() {
		if (!isValuesInited) {
			isValuesInited = true;
			//读取release下的App列表
			Module releaseModule = PluginUtil.getReleaseModule(SuspendAppConfigurable.currentProject, true);
			if (releaseModule == null) {
				return;
			}
			String installPath = releaseModule.getModuleFile().getParent().getPath() + "/apps/install/";
			File installDir = new File(installPath);

			String[] appDirNames = null;
			List<String> appDirNameList = PluginUtil.getAppDirs(installDir);
			Collections.sort(appDirNameList);
			appDirNames = appDirNameList.toArray(new String[] {});

			Object[][] values1 = new Object[appDirNames.length][];
			String[] suspendedAppIds = PropertiesComponent.getInstance().getValues("suspendAppIds");
			List<String> suspendedAppIdList = new ArrayList<>();
			if (suspendedAppIds != null) {
				suspendedAppIdList = Arrays.asList(suspendedAppIds);
			}
			int j = 0;
			for (int i = 0; i < appDirNames.length; i++) {
				String appDirName = appDirNames[i];

				try {
					SAXReader saxreader = new SAXReader();
					String xmlFile = installPath + appDirName + "/manifest.xml";
					Document doc = saxreader.read(xmlFile);
					Element root = doc.getRootElement();
					Object[] value = new Object[4];
					value[0] = appDirName;
					//suspend default false
					value[2] = "自启动";
					if (suspendedAppIdList.contains(appDirName)) {
						value[3] = new Boolean(true);
					} else {
						value[3] = new Boolean(false);
					}
					for (Iterator iter = root.elementIterator(); iter.hasNext(); ) {
						Element element = (Element) iter.next();
						if (element != null) {
							if (element.getName().equals("suspend")) {
								String str = element.getTextTrim();
								if (str.equalsIgnoreCase("true")) {
									value[2] = "已暂停";
								}
							} else if (element.getName().equals("name")) {
								value[1] = element.getTextTrim();
							}
						}
					}
					values1[j] = value;
					j++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			//过滤一遍values1，防止null值（某些文件夹下不存在manifest）
			List<Object[]> objectsList = new ArrayList<>();
			for (Object[] objects : values1) {
				if (objects != null) {
					objectsList.add(objects);
				}
			}

			values = objectsList.toArray(new Object[0][]);
			//如果存在当前状态和设定状态不匹配的，则激活apply按钮
			if (values != null) {
				for (Object[] value : values) {
					String currentState = (String) value[2];
					Boolean setState = (Boolean) value[3];
					if ("自启动".equals(currentState) && setState) {
						SuspendAppConfigurable.isModified = true;
						break;
					} else if ("已暂停".equals(currentState) && !setState) {
						SuspendAppConfigurable.isModified = true;
						break;
					}
				}
			}
		}
	}

	public void fireColorModified() {
		isValuesInited = false;
		initValues();
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				fireTableCellUpdated(i, 1);
			}
		}
	}

	@Override
	public int getRowCount() {
		initValues();
		return values.length;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		initValues();
		return values[rowIndex][columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		values[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
		SuspendAppConfigurable.isModified = true;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

}

