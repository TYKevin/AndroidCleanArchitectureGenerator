package com.github.tykevin.androidcleanarchitecturegenerator.form;

import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.ClassNameUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CleanFastSelector extends JPanel {

    private static JFrame mDialog;

    private final Project project;
    private final Editor editor;
    private final BaseInfo baseInfo;

    protected JButton btnConfirm;
    protected JButton btnCancel;

    public CleanFastSelector(Project project, Editor editor, BaseInfo baseInfo) {
        this.project = project;
        this.editor = editor;
        this.baseInfo = baseInfo;
        setPreferredSize(new Dimension(820, 520));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        if (baseInfo == null) {
            return;
        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));



        addUseCaseInfo();
        addRepositoryListRadioGroup();
        addButtons();
    }

    private void addUseCaseInfo() {
        JLabel labelUseCaseName = new JLabel();
        labelUseCaseName.setText(baseInfo.useCasePsiClass.getName() + " 快速生成");
        add(labelUseCaseName);

        JPanel panelComment = new JPanel();
        JLabel labelComment = new JLabel();
        labelComment.setText("UseCase 的作用：");
        panelComment.add(labelComment);

        JTextField  txtComment = new JTextField(5);
        txtComment.setToolTipText(baseInfo.useCasePsiClass.getName() + "的作用（将用作方法注释）");
        panelComment.add(txtComment);
        add(panelComment);


        JPanel panelReturnType = new JPanel();
        JLabel labelReturnTypeTitle = new JLabel();
        labelReturnTypeTitle.setText("出参：");
        panelReturnType.add(labelReturnTypeTitle);

        JLabel labelReturnType = new JLabel();
        labelReturnType.setText(baseInfo.returnPsiClass != null ? baseInfo.returnPsiClass.getName(): "");
        panelReturnType.add(labelReturnType);
        add(panelReturnType);


        JPanel panelParamType = new JPanel();
        JLabel labelParamTypeTitle = new JLabel();
        labelParamTypeTitle.setText("入参：");
        panelParamType.add(labelParamTypeTitle);

        JLabel labelParamType = new JLabel();
        labelParamType.setText(baseInfo.paramPsiClass != null ?  baseInfo.paramPsiClass.getName() : "");
        panelParamType.add(labelParamType);

        JTextField  txtParamName = new JTextField(5);
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

        for (PsiFile element : baseInfo.repositoryInterfaceFiles) {
            JRadioButton rbRepostory = new JRadioButton(element.getName());
            rbRepostory.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
//                    selectedRepostory = element;
                }
            });
            group.add(rbRepostory);
            panelRepositoryList.add(rbRepostory);

            if (cnt > 0) {
                panelRepositoryList.add(Box.createRigidArea(new Dimension(0, 5)));
            } else {
                rbRepostory.setSelected(true);
            }
            cnt++;
        }

        add(panelRepositoryList);
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

        JPanel buttonPanel = new JPanel();
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



    protected class ConfirmAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {

        }
    }

    protected class CancelAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {

        }
    }

    public static void showSelectorDialog(Project project, Editor editor, BaseInfo baseInfo) {
        mDialog = new JFrame();
        CleanFastSelector selector = new CleanFastSelector(project, editor, baseInfo);
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(selector.getConfirmButton());
        mDialog.setTitle("Clean Arch Fast");
        mDialog.getContentPane().add(selector);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }
}
