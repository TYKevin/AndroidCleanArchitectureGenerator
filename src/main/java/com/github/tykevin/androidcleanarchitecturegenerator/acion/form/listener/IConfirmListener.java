package com.github.tykevin.androidcleanarchitecturegenerator.acion.form.listener;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;

public interface IConfirmListener {

    void onConfirm(Project mProject, Editor mEditor, ArrayList<PsiFile> mElements, PsiFile psiFile);
}
