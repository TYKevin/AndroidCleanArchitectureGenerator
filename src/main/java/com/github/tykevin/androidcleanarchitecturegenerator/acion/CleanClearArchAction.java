package com.github.tykevin.androidcleanarchitecturegenerator.acion;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.NotNull;

public class CleanClearArchAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);

        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        //如果光标选择的不是类，弹出对话框提醒
        if (psiElement == null || !(psiElement instanceof PsiClass)) {
            Messages.showMessageDialog(project, "Please focus on a class", "Generate Failed", null);
            return;
        }

        getFeilds((PsiClass) psiElement, project);
    }

    public void getFeilds(PsiClass psiClass, Project project){
        for (PsiField psiField: psiClass.getFields()) {
            if (psiField.getModifierList().hasExplicitModifier("static")){
                continue;
            }
            if (psiField.getDocComment() != null) {
                StringBuilder commentAccum = new StringBuilder();
                for (PsiElement psiElement : psiField.getDocComment().getDescriptionElements()){
                    commentAccum.append(psiElement.getText());
                }

                Messages.showMessageDialog(project, commentAccum.toString(), "Doc Comment", null);
            }

            Messages.showMessageDialog(project, psiField.getName() , "title" , null);

            String supertypeName = "";
            for (PsiType type:psiField.getType().getSuperTypes()) {
                supertypeName += type.getCanonicalText() + "; ";
            }

            Messages.showMessageDialog(project, supertypeName , "title" , null);
        }
    }
}
