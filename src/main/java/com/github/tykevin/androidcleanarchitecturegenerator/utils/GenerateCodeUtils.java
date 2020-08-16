package com.github.tykevin.androidcleanarchitecturegenerator.utils;

import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.beans.DataStoreImplInfo;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import java.util.LinkedHashMap;

public class GenerateCodeUtils {
    public static void generatorUseCaseCode(Project mProject, BaseInfo baseInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(mProject, () -> {
                PsiClass useCaseClass = baseInfo.useCasePsiClass;
                PsiClass repositoryClass = baseInfo.repositoryInterface;
                PsiClass returnClass = baseInfo.returnPsiClass;
                PsiClass paramClass = baseInfo.paramPsiClass;
                String paramFieldName = baseInfo.getParamFieldName();

                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(mProject);
                useCaseClass.add(mFactory.createFieldFromText("private final " + repositoryClass.getQualifiedName() + " repository;", useCaseClass));

                StringBuilder method = new StringBuilder();
                method.append("@javax.inject.Inject\n");
                method.append("public " + useCaseClass.getName() + "(com.qianmi.arch.domain.executor.ThreadExecutor threadExecutor, com.qianmi.arch.domain.executor.PostExecutionThread postExecutionThread, " + repositoryClass.getQualifiedName() + " repository) { \n");
                method.append("super(threadExecutor, postExecutionThread);\n");
                method.append("this.repository = repository;}");
                useCaseClass.add(mFactory.createMethodFromText(method.toString(), useCaseClass));

                String repositoryFunc = baseInfo.getUseCaseActionFuncName();
                String repositoryReturnType = returnClass.getQualifiedName();
                String repositoryParamType = paramClass.getQualifiedName();
                String repositoryFuncParamName = baseInfo.isVoidParam() ? "v" : paramFieldName;

                StringBuilder methodBuildUseCase = new StringBuilder();
                methodBuildUseCase.append("@Override\n");
                methodBuildUseCase.append("public io.reactivex.Observable<" + repositoryReturnType + "> buildUseCaseObservable(" + repositoryParamType + "  " + repositoryFuncParamName + ")");
                methodBuildUseCase.append("{\n");
                methodBuildUseCase.append(" return this.repository." + repositoryFunc + "(" + paramFieldName + ");\n");
                methodBuildUseCase.append("}");
                useCaseClass.add(mFactory.createMethodFromText(methodBuildUseCase.toString(), useCaseClass));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
                styleManager.optimizeImports(useCaseClass.getContainingFile());
                styleManager.shortenClassReferences(useCaseClass);
                new ReformatCodeProcessor(mProject, useCaseClass.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }

    /**
     * 在 Repository 接口中，定义方法，加上注释
     *
     * @param project
     * @param baseInfo
     */
    public static void generatorRepositoryCode(Project project, BaseInfo baseInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(project);

                PsiClass repositoryInterface = baseInfo.repositoryInterface;
                PsiClass returnClass = baseInfo.returnPsiClass;
                PsiClass paramClass = baseInfo.paramPsiClass;
                String comment = baseInfo.comment;
                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();

                String repositoryReturnClassName = returnClass.getQualifiedName();
                String repositoryParamClassName = paramClass.getQualifiedName();

                StringBuilder method = new StringBuilder();
                method.append("/**\n");
                method.append(" * " + comment + "\n");
                method.append(" */\n");
                method.append("io.reactivex.Observable<" + repositoryReturnClassName + "> " + repositoryFuncName + "(");
                if (!baseInfo.isVoidParam()) {
                    method.append(repositoryParamClassName + " " + paramFieldName);
                }
                method.append(");");
                repositoryInterface.add(mFactory.createMethodFromText(method.toString(), repositoryInterface));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                styleManager.optimizeImports(repositoryInterface.getContainingFile());
                styleManager.shortenClassReferences(repositoryInterface);
                new ReformatCodeProcessor(project, repositoryInterface.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }

    /**
     * 在 Repository 实现类中 添加方法实现
     *
     * @param project
     * @param baseInfo
     */
    public static void generatorRepositoryImplCode(Project project, BaseInfo baseInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(project);
                PsiClass repositoryImplClass = baseInfo.repostoryImplClass;


                PsiClass returnClass = baseInfo.returnPsiClass;
                PsiClass paramClass = baseInfo.paramPsiClass;
                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();
                String dataStoreFieldName = baseInfo.dataStoreFieldName;

                String repositoryReturnClassName = returnClass.getQualifiedName();
                String repositoryParamClassName = paramClass.getQualifiedName();


                StringBuilder method = new StringBuilder();
                method.append("@Override\n");
                method.append("public io.reactivex.Observable<" + repositoryReturnClassName + "> " + repositoryFuncName + "(");
                if (!baseInfo.isVoidParam()) {
                    method.append(repositoryParamClassName + " " + paramFieldName);
                }
                method.append("){");
                method.append("return this.").append(dataStoreFieldName).append(".").append(repositoryFuncName).append("(");
                if (!baseInfo.isVoidParam()) {
                    method.append(paramFieldName);
                }
                method.append(");");
                method.append("}");
                repositoryImplClass.add(mFactory.createMethodFromText(method.toString(), repositoryImplClass));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                styleManager.optimizeImports(repositoryImplClass.getContainingFile());
                styleManager.shortenClassReferences(repositoryImplClass);
                new ReformatCodeProcessor(project, repositoryImplClass.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }

    /**
     * 在 DataStore 接口中，定义方法，加上注释
     *
     * @param project
     * @param baseInfo
     */
    public static void generatorDataStoreCode(Project project, BaseInfo baseInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(project);

                PsiClass dataStoreInterface = baseInfo.dataStoreInterface;
                PsiClass returnClass = baseInfo.returnPsiClass;
                PsiClass paramClass = baseInfo.paramPsiClass;
                String comment = baseInfo.comment;
                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();

                String repositoryReturnClassName = returnClass.getQualifiedName();
                String repositoryParamClassName = paramClass.getQualifiedName();

                StringBuilder method = new StringBuilder();
                method.append("/**\n");
                method.append(" * " + comment + "\n");
                method.append(" */\n");
                method.append("io.reactivex.Observable<" + repositoryReturnClassName + "> " + repositoryFuncName + "(");
                if (!baseInfo.isVoidParam()) {
                    method.append(repositoryParamClassName + " " + paramFieldName);
                }
                method.append(");");
                dataStoreInterface.add(mFactory.createMethodFromText(method.toString(), dataStoreInterface));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                styleManager.optimizeImports(dataStoreInterface.getContainingFile());
                styleManager.shortenClassReferences(dataStoreInterface);
                new ReformatCodeProcessor(project, dataStoreInterface.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }


    /**
     * 在 DataStore 实现类中 添加方法实现
     *
     * @param project
     * @param baseInfo
     */
    public static void generatorDataStoreImplCode(Project project, BaseInfo baseInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(project);
                LinkedHashMap<PsiClass, DataStoreImplInfo> dataStoreImplClassesMap = baseInfo.dataStoreImplClassesMap;
                if (dataStoreImplClassesMap == null) {
                    return;
                }

                for (PsiClass dataStoreImplClass : dataStoreImplClassesMap.keySet()) {
                    DataStoreImplInfo dataStoreImplInfo = dataStoreImplClassesMap.get(dataStoreImplClass);
                    toGeneratorDataStoreImplImplCode(project, mFactory, dataStoreImplClass, dataStoreImplInfo, baseInfo);
                }
            });
        });
    }

    private static void toGeneratorDataStoreImplImplCode(Project project, PsiElementFactory mFactory, PsiClass dataStoreImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo) {
        PsiClass returnClass = baseInfo.returnPsiClass;
        PsiClass paramClass = baseInfo.paramPsiClass;
        String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
        String paramFieldName = baseInfo.getParamFieldName();
        String dataSourceFieldName = dataStoreImplInfo.generateInterfaceFieldName;

        String repositoryReturnClassName = returnClass.getQualifiedName();
        String repositoryParamClassName = paramClass.getQualifiedName();


        StringBuilder method = new StringBuilder();
        method.append("@Override\n");
        method.append("public io.reactivex.Observable<" + repositoryReturnClassName + "> " + repositoryFuncName + "(");
        if (!baseInfo.isVoidParam()) {
            method.append(repositoryParamClassName + " " + paramFieldName);
        }
        method.append("){");
        if (dataStoreImplInfo.isNeedGenerate) {
             method.append("return this.").append(dataSourceFieldName).append(".").append(repositoryFuncName).append("(");
                if (!baseInfo.isVoidParam()) {
                    method.append(paramFieldName);
                }
                method.append(");");
        } else {
            method.append("return null;");
        }
        method.append("}");
        dataStoreImplClass.add(mFactory.createMethodFromText(method.toString(), dataStoreImplClass));

        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
        styleManager.optimizeImports(dataStoreImplClass.getContainingFile());
        styleManager.shortenClassReferences(dataStoreImplClass);
        new ReformatCodeProcessor(project, dataStoreImplClass.getContainingFile(), null, false).runWithoutProgress();
    }


}
