package com.github.tykevin.androidcleanarchitecturegenerator.form;

import com.github.tykevin.androidcleanarchitecturegenerator.acion.CleanArchFast;
import com.github.tykevin.androidcleanarchitecturegenerator.acion.utils.Utils;
import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.beans.DataStoreImplInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.ClassNameUtils;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.MessageUtils;
import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class CleanFastSelector extends JPanel {
    private static final Logger log = Logger.getInstance(CleanArchFast.class);

    private static JFrame mDialog;

    private final Project project;
    private final Editor editor;
    private final BaseInfo baseInfo;
    private final ActionListener actionListener;

    private JTextField txtParamName;
    private JTextField txtComment;

    private JPanel buttonPanel;
    protected JButton btnConfirm;
    protected JButton btnCancel;

    protected PsiFile selectedRepostoryFile;

    protected ArrayList<ButtonModel> dsImplButtonModelList;


    public CleanFastSelector(Project project, Editor editor, BaseInfo baseInfo, ActionListener actionListener) {
        this.project = project;
        this.editor = editor;
        this.baseInfo = baseInfo;
        this.actionListener = actionListener;

        setPreferredSize(new Dimension(820, 520));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (baseInfo == null) {
            return;
        }

        addUseCaseInfo();
        addRepositoryListRadioGroup();
    }

    private void addUseCaseInfo() {
        JPanel panelComment = new JPanel();
        JLabel labelComment = new JLabel();
        labelComment.setText(baseInfo.useCasePsiClass.getName() + "的作用：");
        panelComment.add(labelComment);

        txtComment = new JTextField(5);
        txtComment.setToolTipText(baseInfo.useCasePsiClass.getName() + "的作用（将用作方法注释）");
        panelComment.add(txtComment);
        add(panelComment);


        JPanel panelReturnType = new JPanel();
        JLabel labelReturnTypeTitle = new JLabel();
        labelReturnTypeTitle.setText("出参：");
        panelReturnType.add(labelReturnTypeTitle);

        JLabel labelReturnType = new JLabel();
        labelReturnType.setText(baseInfo.returnPsiClassFullName);
        panelReturnType.add(labelReturnType);
        add(panelReturnType);


        JPanel panelParamType = new JPanel();
        JLabel labelParamTypeTitle = new JLabel();
        labelParamTypeTitle.setText("入参：");
        panelParamType.add(labelParamTypeTitle);

        JLabel labelParamType = new JLabel();
        labelParamType.setText(baseInfo.paramPsiClassFullName);
        panelParamType.add(labelParamType);

        txtParamName = new JTextField(5);
        txtParamName.setText(baseInfo.paramPsiClass != null ? ClassNameUtils.subClassNameToFuncName(baseInfo.paramPsiClass.getName()) : "");
        panelParamType.add(txtParamName);
        add(panelParamType);
    }


    private void addRepositoryListRadioGroup() {
        JLabel labelSelectRepository = new JLabel();
        labelSelectRepository.setText("选择对应的 repository: ");
        add(labelSelectRepository);

        if (baseInfo.repositoryInterfaceFiles == null) {
            return;
        }

        JPanel panelRepositoryList = new JPanel();
        panelRepositoryList.setLayout(new BoxLayout(panelRepositoryList, BoxLayout.PAGE_AXIS));

        int cnt = 0;
        ButtonGroup group = new ButtonGroup();
        ArrayList<ButtonModel> radioButtonModelList = new ArrayList<>();
        for (PsiFile element : baseInfo.repositoryInterfaceFiles) {
            JRadioButton rbRepostory = new JRadioButton(element.getName());
            group.add(rbRepostory);
            panelRepositoryList.add(rbRepostory);

            rbRepostory.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (!rbRepostory.isSelected()) {
                        return;
                    }

                    CleanFastSelector.this.selectedRepostoryFile = element;
                    if (selectedRepostoryFile == null) {
                        return;
                    }

                    log.info("selectedRepostory = " + selectedRepostoryFile.getName());
                    selectRepostoryFile();

                }
            });

            if (cnt > 0) {
                panelRepositoryList.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            cnt++;

            radioButtonModelList.add(rbRepostory.getModel());
        }
        add(panelRepositoryList);

        if (radioButtonModelList != null && radioButtonModelList.size() > 0) {
            group.setSelected(radioButtonModelList.get(0), true);
        }
    }

    private void selectRepostoryFile() {
        // 获取选择的 repository 的 PsiClass 对象
        baseInfo.repositoryInterface = PsiTreeUtil.findChildOfAnyType(selectedRepostoryFile.getOriginalElement(), PsiClass.class);
        if (baseInfo.repositoryInterface == null) {
            MessageUtils.showErrorMsg(project, selectedRepostoryFile.getName() + " 没有 class！");
            return;
        }

        baseInfo.repostoryFieldMap = getDataStoreFieldMap(baseInfo.repositoryInterface);
        addDataStoreSelector();
    }

    JPanel panelDataStoreMap;
    JLabel labelSelectDataStore;

    /**
     * 选择 DataStore
     */
    private void addDataStoreSelector() {
        if (panelDataStoreMap != null) {
            remove(panelDataStoreMap);
            remove(labelSelectDataStore);

            if (buttonPanel != null) {
                remove(buttonPanel);
            }
            if (labelSelectDataStoreImpl != null) {
                remove(labelSelectDataStoreImpl);
            }
            if (panelDataStoreImplList != null) {
                if (panelDsGenerateTips != null) {
                    // 清空生成信息
                    panelDataStoreImplList.remove(panelDsGenerateTips);
                }
                remove(panelDataStoreImplList);
            }
            revalidate();
        }

        if (baseInfo.repostoryFieldMap == null) {
            MessageUtils.showErrorMsg(project, "repostory 中未找到对应的 DataStore 实例！");
            return;
        }

        labelSelectDataStore = new JLabel();
        labelSelectDataStore.setText("选择需要生成代码的 DataStore: ");
        add(labelSelectDataStore);

        panelDataStoreMap = new JPanel();
        panelDataStoreMap.setLayout(new BoxLayout(panelDataStoreMap, BoxLayout.PAGE_AXIS));
        int cnt = 0;
        ButtonGroup group = new ButtonGroup();
        ArrayList<ButtonModel> radioButtonModelList = new ArrayList<>();
        for (String fieldName : baseInfo.repostoryFieldMap.keySet()) {
            JRadioButton rbDataStrore = new JRadioButton(fieldName);
            group.add(rbDataStrore);
            panelDataStoreMap.add(rbDataStrore);

            rbDataStrore.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (!rbDataStrore.isSelected()) {
                        return;
                    }

                    log.info("selectedDataStore = " + fieldName);
                    baseInfo.dataStoreFieldName = fieldName;
                    baseInfo.dataStoreInterface = baseInfo.repostoryFieldMap.get(fieldName);
                    if (baseInfo.dataStoreInterface == null) {
                        MessageUtils.showErrorMsg(project, "没有找到DataSource PsiClass");
                        return;
                    }

                    addDataStoreImplChecker();
                }
            });

            if (cnt > 0) {
                panelDataStoreMap.add(Box.createRigidArea(new Dimension(0, 5)));
            }

            radioButtonModelList.add(rbDataStrore.getModel());
            cnt++;
        }
        add(panelDataStoreMap);

        if (radioButtonModelList != null && radioButtonModelList.size() > 0) {
            group.setSelected(radioButtonModelList.get(0), true);
        }
    }

    JLabel labelSelectDataStoreImpl;
    JPanel panelDataStoreImplList;


    /**
     * 选择 DataStore Impl
     * 未勾选，则直接 return
     * 勾选：
     * Net：选择 api，继续创建接口请求代码
     * Cache： 选择 DB，继续创建数据库模板代码
     *
     * todo：baseInfo 数据源获取分离
     */
    private void addDataStoreImplChecker() {
        if (labelSelectDataStoreImpl != null) {
            remove(labelSelectDataStoreImpl);
        }
        if (panelDataStoreImplList != null) {
            if (panelDsGenerateTips != null) {
                // 清空生成信息
                panelDataStoreImplList.remove(panelDsGenerateTips);
            }
            remove(panelDataStoreImplList);
        }


        if (buttonPanel != null) {
            remove(buttonPanel);
        }
        revalidate();


        //  获取接口实现类
        PsiClass[] dataStoreImplClasses = Utils.getImplClasses(baseInfo.dataStoreInterface);
        if (dataStoreImplClasses == null || dataStoreImplClasses.length <= 0) {
            Messages.showMessageDialog(project, "未找到 DataStore 实现类", "错误", null);
            return;
        }

        LinkedHashMap<PsiClass, DataStoreImplInfo> dataSourceImplClassesMap = new  LinkedHashMap<PsiClass, DataStoreImplInfo>();
        for (PsiClass psiClass : dataStoreImplClasses) {
            log.info("dataSourceImplClass = " + psiClass);
            dataSourceImplClassesMap.put(psiClass, new DataStoreImplInfo());
        }
        baseInfo.dataStoreImplClassesMap = dataSourceImplClassesMap;

         // 添加界面
        labelSelectDataStoreImpl = new JLabel();
        labelSelectDataStoreImpl.setText("检测到数据源: ");
        add(labelSelectDataStoreImpl);

        panelDataStoreImplList = new JPanel();
        panelDataStoreImplList.setLayout(new BoxLayout(panelDataStoreImplList, BoxLayout.PAGE_AXIS));

        int cnt = 0;
        dsImplButtonModelList = new ArrayList<>();
        for (PsiClass dataStoreImplClass : baseInfo.dataStoreImplClassesMap.keySet()) {
            JCheckBox cbDataStroreImpl = new JCheckBox(dataStoreImplClass.getName());
            panelDataStoreImplList.add(cbDataStroreImpl);
            dsImplButtonModelList.add(cbDataStroreImpl.getModel());
            cbDataStroreImpl.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (baseInfo.dataStoreInterface == null) {
                        MessageUtils.showErrorMsg(project, "没有找到DataSource PsiClass");
                        return;
                    }

                    if (dataSourceImplClassesMap.containsKey(dataStoreImplClass)) {
                        DataStoreImplInfo dataStoreImplInfo = dataSourceImplClassesMap.get(dataStoreImplClass);
                        dataStoreImplHandler(dataStoreImplInfo, cbDataStroreImpl, dataStoreImplClass);
                        dataSourceImplClassesMap.replace(dataStoreImplClass, dataStoreImplInfo);
                    } else {
                        DataStoreImplInfo dataStoreImplInfo = new DataStoreImplInfo();
                        dataStoreImplHandler(dataStoreImplInfo, cbDataStroreImpl, dataStoreImplClass);
                        dataSourceImplClassesMap.put(dataStoreImplClass,dataStoreImplInfo);
                    }
                }

                /**
                 * 根据 dataStoreImpl 的选中状态进行处理
                 */
                private void dataStoreImplHandler(final DataStoreImplInfo dataStoreImplInfo, JCheckBox cbDataStroreImpl, PsiClass dataStoreImplClass) {
                    dataStoreImplInfo.isNeedGenerate = cbDataStroreImpl.isSelected();
                    if (!dataStoreImplInfo.isNeedGenerate) {
                        dataStoreImplInfo.generateDataSourceInterface = null;
                        dataStoreImplInfo.generateInterfaceFieldName = null;
                        dataStoreImplInfo.generateType = null;
                        addOrRefreshGenerateTips();
                    } else {
                        // 如果需要生成模板代码，弹出生成模板选择框
                        DataSourceGenerateSelector.showSelectorDialog(project, editor,
                                dataStoreImplClass, dataStoreImplInfo, new DataSourceGenerateSelector.ActionListener() {
                                    @Override
                                    public void onConfirmAction(DataStoreImplInfo info) {
                                        addOrRefreshGenerateTips();
                                    }

                                    @Override
                                    public void onCancelAction() {
                                        dataStoreImplInfo.isNeedGenerate = false;
                                        dataStoreImplInfo.generateDataSourceInterface = null;
                                        dataStoreImplInfo.generateInterfaceFieldName = null;
                                        dataStoreImplInfo.generateType = null;
                                        cbDataStroreImpl.setSelected(false);
                                        addOrRefreshGenerateTips();
                                    }
                                }
                        );
                    }
                }
            });

            if (cnt > 0) {
                panelDataStoreImplList.add(Box.createRigidArea(new Dimension(0, 5)));
            }
            cnt++;
        }


        addOrRefreshGenerateTips();
        add(panelDataStoreImplList);
        addButtons();
        refresh();

    }

    JPanel panelDsGenerateTips;
    private void addOrRefreshGenerateTips() {
        if (panelDsGenerateTips != null) {
            panelDsGenerateTips.removeAll();
            if(panelDataStoreImplList != null) {
                panelDataStoreImplList.remove(panelDsGenerateTips);
            }

            revalidate();
        }

        panelDsGenerateTips = new JPanel();
        panelDataStoreImplList.add(panelDsGenerateTips);
        LinkedHashMap<PsiClass, DataStoreImplInfo> dataStoreImplClassesMap  = baseInfo.dataStoreImplClassesMap;
        StringBuilder stringBuilder;
        for (PsiClass dataStoreImplClass: dataStoreImplClassesMap.keySet()) {
            JLabel labelTips = new JLabel();
            DataStoreImplInfo dataStoreImplInfo =  dataStoreImplClassesMap.get(dataStoreImplClass);
            stringBuilder = new StringBuilder();
            stringBuilder.append(dataStoreImplClass.getName()).append(":");

            if (dataStoreImplInfo == null) {
                stringBuilder.append("无法生成; \n");
                labelTips.setText(stringBuilder.toString());
                panelDsGenerateTips.add(labelTips);
                continue;
            }

            if (!dataStoreImplInfo.isNeedGenerate) {
                stringBuilder.append("未选择，将在 DataStore 实现类中返回null; \n");
                labelTips.setText(stringBuilder.toString());
                panelDsGenerateTips.add(labelTips);
                continue;
            }

            stringBuilder.append("在 ");
            if (dataStoreImplInfo.generateDataSourceInterface != null) {
                stringBuilder. append(dataStoreImplInfo.generateDataSourceInterface.getName());
            }

            stringBuilder.append(" 中生成");
            if (dataStoreImplInfo.generateType != null) {
                stringBuilder.append(dataStoreImplInfo.generateType.desc);
            }

            stringBuilder.append("数据的模板代码; \n");
            labelTips.setText(stringBuilder.toString());
            panelDsGenerateTips.add(labelTips);
        }

        revalidate();
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

    public JButton getConfirmButton() {
        return btnConfirm;
    }


    protected void refresh() {
        revalidate();

        if (btnConfirm != null) {
            btnConfirm.setEnabled(baseInfo != null);
        }
    }

    public Map<String, PsiClass> getDataStoreFieldMap(PsiClass repositoryInterface) {
        //  获取接口实现类
        PsiClass[] repostoryImplClasses = Utils.getImplClasses(repositoryInterface);
        if (repostoryImplClasses == null || repostoryImplClasses.length <= 0) {
            Messages.showMessageDialog(project, "未找到 repostory 实现类", "错误", null);
            return null;
        }

        PsiClass repostoryImplClass = repostoryImplClasses[0];
        baseInfo.repostoryImplClass = repostoryImplClass;

        PsiField[] repostoryFields = repostoryImplClass.getFields();
        if (repostoryFields == null || repostoryFields.length <= 0) {
            Messages.showMessageDialog(project, "repostory 实现类中未找到 DataStore Fields", "错误", null);
            return null;
        }

        Map<String, PsiClass> repostoryFieldMap = new LinkedHashMap<>();
        for (PsiField repostoryImplFields : repostoryFields) {
            log.info("repostoryImplFields = " + repostoryImplFields.getName());
            PsiClass fieldClass = PsiTypesUtil.getPsiClass(repostoryImplFields.getType());
            if (fieldClass == null) {
                continue;
            }

            log.info("repostoryImplFields class: " + fieldClass.getQualifiedName());
            log.info("repostoryImplFields class kind: " + fieldClass.getClassKind());

            String fieldName = repostoryImplFields.getName();
            String fieldClassName = fieldClass.getName();
            JvmClassKind fieldClassKind = fieldClass.getClassKind();

            // todo 支持 factory 生成: ( fieldClassName.toLowerCase().contains("factory") && fieldClassKind == JvmClassKind.CLASS) ||
            boolean isDataStoreRef = fieldClassKind == JvmClassKind.INTERFACE;

            if (isDataStoreRef) {
                repostoryFieldMap.put(fieldName, fieldClass);
            }
        }

        return repostoryFieldMap;
    }

    public static void showSelectorDialog(Project project, Editor editor, BaseInfo baseInfo, ActionListener actionListener) {
        mDialog = new JFrame();
        CleanFastSelector selector = new CleanFastSelector(project, editor, baseInfo, actionListener);
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(selector.getConfirmButton());
        mDialog.setTitle("Clean Arch Fast");
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


    protected class ConfirmAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            baseInfo.comment = txtComment.getText();
            if (baseInfo.comment == null
                    || baseInfo.comment.length() <= 0) {
                MessageUtils.showErrorMsg(project, "请输入方法注释");
                return;
            }

            baseInfo.paramFieldName = txtParamName.getText();
            if (baseInfo.paramFieldName == null
                    || baseInfo.paramFieldName.length() <= 0) {
                baseInfo.paramFieldName = ClassNameUtils.subClassNameToFuncName(baseInfo.paramPsiClass.getName());
            }

            if (actionListener != null) {
                actionListener.onConfirmAction(baseInfo);
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
        void onConfirmAction(BaseInfo info);
        void onCancelAction();
    }
}
