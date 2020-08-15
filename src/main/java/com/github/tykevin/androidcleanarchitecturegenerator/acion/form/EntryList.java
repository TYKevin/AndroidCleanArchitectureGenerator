package com.github.tykevin.androidcleanarchitecturegenerator.acion.form;

import com.github.tykevin.androidcleanarchitecturegenerator.acion.form.listener.ICancelListener;
import com.github.tykevin.androidcleanarchitecturegenerator.acion.form.listener.IConfirmListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

public class EntryList extends JPanel {

    protected Project mProject;
    protected Editor mEditor;
    protected ArrayList<PsiFile> mElements = new ArrayList<PsiFile>();
    protected JButton mConfirm;
    protected JButton mCancel;

    protected IConfirmListener mConfirmListener;
    protected ICancelListener mCancelListener;

    protected PsiFile selectedRepostory;

    public EntryList(Project project, Editor editor, ArrayList<PsiFile> elements, IConfirmListener confirmListener, ICancelListener cancelListener) {
        mProject = project;
        mEditor = editor;
        mConfirmListener = confirmListener;
        mCancelListener = cancelListener;
        if (elements != null) {
            mElements = elements;
        }

        setPreferredSize(new Dimension(420, 220));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        addInjections();
        addButtons();
    }

    protected void addInjections() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel injectionsPanel = new JPanel();
        injectionsPanel.setLayout(new BoxLayout(injectionsPanel, BoxLayout.PAGE_AXIS));
        injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        int cnt = 0;
        ButtonGroup group = new ButtonGroup();

        for (PsiFile element : mElements) {
            JRadioButton rbRepostory = new JRadioButton(element.getName());
            rbRepostory.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    selectedRepostory = element;
                }
            });
            group.add(rbRepostory);
            injectionsPanel.add(rbRepostory);

            if (cnt > 0) {
                injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            } else {
                rbRepostory.setSelected(true);
            }

            cnt++;
        }


        injectionsPanel.add(Box.createVerticalGlue());
        injectionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        JBScrollPane scrollPane = new JBScrollPane(injectionsPanel);
        contentPanel.add(scrollPane);
        add(contentPanel, BorderLayout.CENTER);
        refresh();
    }

    protected void addButtons() {
        mCancel = new JButton();
        mCancel.setAction(new CancelAction());
        mCancel.setPreferredSize(new Dimension(120, 26));
        mCancel.setText("取消");
        mCancel.setVisible(true);

        mConfirm = new JButton();
        mConfirm.setAction(new ConfirmAction());
        mConfirm.setPreferredSize(new Dimension(120, 26));
        mConfirm.setText("确定");
        mConfirm.setVisible(true);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(mCancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(mConfirm);

        add(buttonPanel, BorderLayout.PAGE_END);
        refresh();
    }

    protected void refresh() {
        revalidate();

        if (mConfirm != null) {
            mConfirm.setVisible(mElements.size() > 0);
        }
    }

    public JButton getConfirmButton() {
        return mConfirm;
    }

    protected class ConfirmAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (mConfirmListener != null) {
                mConfirmListener.onConfirm(mProject, mEditor, mElements, selectedRepostory);
            }
        }
    }

    protected class CancelAction extends AbstractAction {

        public void actionPerformed(ActionEvent event) {
            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }
    }
}
