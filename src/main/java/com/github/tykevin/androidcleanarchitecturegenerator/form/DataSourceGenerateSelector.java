package com.github.tykevin.androidcleanarchitecturegenerator.form;

import com.github.tykevin.androidcleanarchitecturegenerator.beans.DataStoreImplInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.MessageUtils;
import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiTypesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataSourceGenerateSelector extends JPanel {
    private static final Logger log = Logger.getInstance(DataSourceGenerateSelector.class);

    private static JFrame mDialog;

    private final Project project;
    private final Editor editor;
    private final PsiClass dataStoreImplClass;
    private final DataStoreImplInfo dataStoreImplInfo;

    private JPanel buttonPanel;
    protected JButton btnConfirm;
    protected JButton btnCancel;

    protected ActionListener actionListener;

    public DataSourceGenerateSelector(Project project, Editor editor, PsiClass dataStoreImplClass, DataStoreImplInfo dataStoreImplInfo, ActionListener actionListener) {
        this.project = project;
        this.editor = editor;
        this.dataStoreImplClass = dataStoreImplClass;
        this.dataStoreImplInfo = dataStoreImplInfo;
        this.actionListener = actionListener;

        setPreferredSize(new Dimension(820, 520));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        if (dataStoreImplInfo == null || dataStoreImplClass == null) {
            return;
        }

        addInfoPanel();
        addSelectGenerateTypePanel();
        addSelectGenerateInterfacePanel();

        addButtons();
    }

    private void addInfoPanel() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("为");
        stringBuilder.append(dataStoreImplClass.getName());
        stringBuilder.append("中的 DataSource 生成模板代码");

        JLabel labelSelectGenType = new JLabel();
        labelSelectGenType.setText(stringBuilder.toString());
        add(labelSelectGenType);
    }

    /**
     * 添加生成模板类型选择模块
     */
    private void addSelectGenerateTypePanel() {
        DataStoreImplInfo.GenerateType[] generateTypeList = DataStoreImplInfo.GenerateType.values();

        JLabel labelSelectGenType = new JLabel();
        labelSelectGenType.setText("选择生成模板类型: ");
        add(labelSelectGenType);

        JPanel panelTypeList = new JPanel();
        panelTypeList.setLayout(new BoxLayout(panelTypeList, BoxLayout.PAGE_AXIS));

        int cnt = 0;
        ButtonGroup group = new ButtonGroup();
        ArrayList<ButtonModel> radioButtonModelList = new ArrayList<>();
        for (DataStoreImplInfo.GenerateType type : generateTypeList) {
            JRadioButton rbType = new JRadioButton(type.desc);
            group.add(rbType);
            panelTypeList.add(rbType);

            rbType.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (!rbType.isSelected()) {
                        return;
                    }

                    dataStoreImplInfo.generateType = type;
                }
            });

            if (cnt > 0) {
                panelTypeList.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            cnt++;

            radioButtonModelList.add(rbType.getModel());
        }
        add(panelTypeList);

        if (radioButtonModelList != null && radioButtonModelList.size() > 0) {
            group.setSelected(radioButtonModelList.get(0), true);
        }
    }
    /**
     * 添加生成模板的接口选择模块
     */
    private void addSelectGenerateInterfacePanel() {
        // 获取数据源： 获取当前 dataSource 下所有interface 类型
        PsiField[] datasourceFields = dataStoreImplClass.getFields();
        if (datasourceFields == null || datasourceFields.length <= 0) {
            MessageUtils.showErrorMsg(project, "DataStore 实现类中未找到 数据源获取接口");
            return;
        }

        LinkedHashMap<String,PsiClass> datasourceFieldMap = new LinkedHashMap<>();
        for (PsiField datasourceImplFields : datasourceFields) {
            log.info("datasourceImplFields = " + datasourceImplFields.getName());
            PsiClass fieldClass = PsiTypesUtil.getPsiClass(datasourceImplFields.getType());
            if (fieldClass == null) {
                continue;
            }

            log.info("datasourceImplFields class: " + fieldClass.getQualifiedName());
            log.info("datasourceImplFields class kind: " + fieldClass.getClassKind());

            JvmClassKind fieldClassKind = fieldClass.getClassKind();

            // 只支持 DataStoreImpl 下 interface 类型的 datasource;
            boolean isDataSourceRef =
                    (fieldClassKind == JvmClassKind.INTERFACE);
            if (isDataSourceRef) {
                datasourceFieldMap.put(datasourceImplFields.getName(), fieldClass);
            }
        }


        // 添加界面
        JLabel labelSelectGenType = new JLabel();
        labelSelectGenType.setText("选择需要生成的接口: ");
        add(labelSelectGenType);

        JPanel panelInterfaceList = new JPanel();
        panelInterfaceList.setLayout(new BoxLayout(panelInterfaceList, BoxLayout.PAGE_AXIS));

        int cnt = 0;
        ButtonGroup group = new ButtonGroup();
        ArrayList<ButtonModel> radioButtonModelList = new ArrayList<>();
        for (String fieldName : datasourceFieldMap.keySet()) {
            PsiClass dataSourceInterface = datasourceFieldMap.get(fieldName);
            JRadioButton rbInterface = new JRadioButton(dataSourceInterface.getName() + " " + fieldName);
            group.add(rbInterface);
            panelInterfaceList.add(rbInterface);

            rbInterface.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (!rbInterface.isSelected()) {
                        return;
                    }
                    log.info("selected data source interface = " + dataSourceInterface.getName() );
                    dataStoreImplInfo.generateInterfaceFieldName = fieldName;
                    dataStoreImplInfo.generateDataSourceInterface = dataSourceInterface;
                }
            });

            if (cnt > 0) {
                panelInterfaceList.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            radioButtonModelList.add(rbInterface.getModel());
            cnt++;
        }
        add(panelInterfaceList);

        if (radioButtonModelList != null && radioButtonModelList.size() > 0) {
            group.setSelected(radioButtonModelList.get(0), true);
        }
    }


    public static void showSelectorDialog(Project project, Editor editor,PsiClass dataStoreImplClass, DataStoreImplInfo dataStoreImplInfo, ActionListener actionListener) {
        mDialog = new JFrame();
        DataSourceGenerateSelector selector = new DataSourceGenerateSelector(project, editor, dataStoreImplClass, dataStoreImplInfo, actionListener);
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        mDialog.getRootPane().setDefaultButton(selector.getConfirmButton());
        mDialog.setUndecorated(true);
        mDialog.setTitle("生成模板代码");
        mDialog.getContentPane().add(selector);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    public static void closeDialog() {
        if (mDialog == null) {
            return;
        }

        mDialog.setVisible(false);
        mDialog.dispose();
    }

    public JButton getConfirmButton() {
        return btnConfirm;
    }

    protected void addButtons() {
        btnCancel = new JButton();
        btnCancel.setAction(new CancelAction());
        btnCancel.setPreferredSize(new Dimension(120, 26));
        btnCancel.setText("取消");
        btnCancel.setVisible(true);

        btnConfirm = new JButton();
        btnConfirm.setAction(new ConfirmAction());
        btnConfirm.setPreferredSize(new Dimension(120, 26));
        btnConfirm.setText("确定");
        btnConfirm.setVisible(true);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(btnCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(btnConfirm);

        add(buttonPanel, BorderLayout.SOUTH);
        refresh();
    }

    protected void refresh() {
        revalidate();

        if (btnConfirm != null) {
            btnConfirm.setEnabled(dataStoreImplInfo != null);
        }
    }



    protected class ConfirmAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            closeDialog();
            if (actionListener != null) {
                actionListener.onConfirmAction(dataStoreImplInfo);
            }
        }
    }

    protected class CancelAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            closeDialog();
            if (actionListener != null) {
                actionListener.onCancelAction();
            }
        }
    }

    public interface ActionListener {
        void onConfirmAction(DataStoreImplInfo info);
        void onCancelAction();
    }


}
