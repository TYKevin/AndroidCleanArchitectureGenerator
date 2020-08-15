package com.github.tykevin.androidcleanarchitecturegenerator.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;

import java.util.Arrays;

public class BaseInfo {

    // UseCase PsiClass
    public PsiClass useCasePsiClass;

    // 出入参 的 PsiType
    public PsiType returnType;
    public PsiType paramType;

    public PsiClass returnPsiClass;
    public PsiClass paramPsiClass;

    // domain/repository 下找到 所有的 interface files
    public PsiFile[] repositoryInterfaceFiles;

    @Override
    public String toString() {
        return "BaseInfo{" +
                "useCasePsiClass=" + useCasePsiClass.getName() +
                ", returnType=" + returnType.getCanonicalText() +
                ", paramType=" + paramType.getCanonicalText() +
                ", repositoryInterfaceFiles=" + Arrays.toString(repositoryInterfaceFiles) +
                '}';
    }
}
