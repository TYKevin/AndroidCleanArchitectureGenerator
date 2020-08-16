package com.github.tykevin.androidcleanarchitecturegenerator.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaseInfo {

    // UseCase PsiClass
    public PsiClass useCasePsiClass;

    // 出入参 的 PsiType
    public PsiType returnType;
    public PsiType paramType;

    public PsiClass returnPsiClass;
    public PsiClass paramPsiClass;

    /**
     * domain/repository 下找到 所有的 interface files
     */
    public PsiFile[] repositoryInterfaceFiles;

    /**
     *  repository 的 interface
     */
    public PsiClass repositoryInterface;
    /**
     *  repository impl 的 class
     */
    public PsiClass repostoryImplClass;

    /**
     * dataStore 的 interface
     */
    public PsiClass dataStoreInterface;

    /**
     * dataStore 的 实现类，以及其中需要生成 DataSource 模板代码的相关信息
     */
    public LinkedHashMap<PsiClass, DataStoreImplInfo> dataStoreImplClassesMap;

    /**
     * repositoryImpl 下的 DataStore 引用
     *
     * 在 Repository 实现类中， type 为 interface 的 DataStore /Factory Class 变量
     *  1 只有 1个 DataStore 实例
     *  2 没有 DataStore，只有 Factory的 实例
     *  3 有 好几个 DataStore 实例
     */
   public Map<String, PsiClass> repostoryFieldMap;

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
