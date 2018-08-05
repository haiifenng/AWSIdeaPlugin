package com.actionsoft.ideaplugins.manifest.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import com.actionsoft.ideaplugins.manifest.SuspendAppAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

/**
 * @author wangshibao
 *         Created on 2017-5-17
 */
public class SuspendAppConfigurable implements Configurable {
	public static Project currentProject = null;
	//是否修改了值
	public static boolean isModified;

	public SuspendAppConfigurable(Project project) {
		isModified = false;
		currentProject = project;
	}

	@Nls
	@Override
	public String getDisplayName() {
		return "AWS Suspend App";
	}

	JPanel panel;
	private JTable appListTable;

	@Nullable
	@Override
	public JComponent createComponent() {

		String appListFilePath = PropertiesComponent.getInstance().getValue("appListFilePath");

		panel = new JPanel();
		panel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));

		appListTable = new JBTable(new AppListTableModel());
		appListTable.setRowHeight(28);

		final JScrollPane scrollPane1 = new JBScrollPane();
		panel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		scrollPane1.setViewportView(appListTable);

		//如果设置值和当前状态值不一样，则显示未醒目的红色
		appListTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

				Component render = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (column == 1) {
					String currentState = (String) table.getValueAt(row, 2);
					Boolean setState = (Boolean) table.getValueAt(row, 3);
					if ("自启动".equals(currentState) && setState) {
						render.setBackground(new Color(249, 123, 121));
						render.setForeground(Color.WHITE);
					} else if ("已暂停".equals(currentState) && !setState) {
						render.setBackground(new Color(249, 123, 121));
						render.setForeground(Color.WHITE);
					} else {
						render.setBackground(table.getBackground());
						render.setForeground(table.getForeground());
					}
				} else {
					render.setBackground(table.getBackground());
					render.setForeground(table.getForeground());
				}
				if (isSelected) {
					render.setBackground(new Color(56, 117, 214));
					render.setForeground(Color.WHITE);
				}
				return render;
			}
		});

		//设置列宽
		appListTable.getColumn("当前状态").setMinWidth(120);
		appListTable.getColumn("当前状态").setMaxWidth(120);
		appListTable.getColumn("设定状态").setMinWidth(120);
		appListTable.getColumn("设定状态").setMaxWidth(120);

		//设置对齐
		DefaultTableCellRenderer cellRenderer3 = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if ("自启动".equals(value)) {
					cell.setBackground(new Color(77, 190, 180));
					cell.setForeground(Color.WHITE);
				} else {
					cell.setBackground(table.getBackground());
					cell.setForeground(table.getForeground());
				}
				return cell;
			}
		};
		cellRenderer3.setHorizontalAlignment(SwingConstants.CENTER);
		appListTable.getColumn("当前状态").setCellRenderer(cellRenderer3);

		//鼠标点击事件
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = appListTable.getSelectedRow();
				int column = appListTable.getSelectedColumn();
				if (column == 3) {
					AppListTableModel tableModel = (AppListTableModel) appListTable.getModel();
					Boolean value = (Boolean) tableModel.getValueAt(row, column);
					//点击checkbox已经修改了值，此处触发一下fireTableCellUpdated
					tableModel.setValueAt(value, row, column);
				}
			}
		};
		appListTable.addMouseListener(mouseAdapter);

		//表头排序
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(appListTable.getModel());
		Comparator<String> appComparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				Collator instance = Collator.getInstance(Locale.CHINA);
				return instance.compare(s1, s2);
			}
		};
		sorter.setComparator(0, appComparator);
		sorter.setComparator(1, appComparator);
		sorter.addRowSorterListener(new RowSorterListener() {
			@Override
			public void sorterChanged(RowSorterEvent e) {
				appListTable.clearSelection();
			}
		});
		appListTable.setRowSorter(sorter);
		appListTable.updateUI();

		appListTable.setVerifyInputWhenFocusTarget(true);

		//键盘space事件
		appListTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 32) {
					int[] rows = appListTable.getSelectedRows();
					if (rows != null) {
						for (int i = 0; i < rows.length; i++) {
							AppListTableModel tableModel = (AppListTableModel) appListTable.getModel();
							Boolean value = (Boolean) tableModel.getValueAt(rows[i], 3);
							tableModel.setValueAt(new Boolean(!value), rows[i], 3);
						}
					}
				}
			}
		});

		return panel;
	}

	@Override
	public boolean isModified() {
		return isModified;
	}

	@Override
	public void apply() throws ConfigurationException {
		AppListTableModel tableModel = (AppListTableModel) appListTable.getModel();
		Object[][] values = tableModel.values;
		java.util.List<String> propertyList = new ArrayList<>();
		int j = 0;
		for (int i = 0; i < values.length; i++) {
			Object[] value = values[i];
			String appId = (String) value[0];
			boolean isSuspend = (Boolean) value[3];
			if (isSuspend) {
				propertyList.add(appId);
			}
		}
		PropertiesComponent.getInstance().setValues("suspendAppIds", propertyList.toArray(new String[0]));

		//修改文件
		SuspendAppAction suspendAppAction = new SuspendAppAction();
		suspendAppAction.actionPerformed();

		isModified = false;
		//重新渲染table的颜色
		((AppListTableModel) appListTable.getModel()).fireColorModified();

	}
}
