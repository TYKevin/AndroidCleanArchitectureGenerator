package com.github.tykevin.androidcleanarchitecturegenerator.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaseInfo {

    /**
     * Usecase 注释
     */
    public String comment;

    /**
     * 入参名称，支持自定义，输入框为空则使用默认
     */
    public String paramFieldName;

    // UseCase PsiClass
    public PsiClass useCasePsiClass;

    // 出入参 的 PsiClass
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
                ", repositoryInterfaceFiles=" + Arrays.toString(repositoryInterfaceFiles) +
                '}';
    }

    public boolean isNoParam() {
        if (paramPsiClass == null) {
            return false;
        }

        String repositoryParamType = this.paramPsiClass.getQualifiedName();
        return "java.lang.Void".equals(repositoryParamType);
    }
}
