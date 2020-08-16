package com.github.tykevin.androidcleanarchitecturegenerator.utils;

import com.github.tykevin.androidcleanarchitecturegenerator.acion.utils.Utils;
import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.beans.DataStoreImplInfo;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
                String repositoryReturnType = baseInfo.returnPsiClassFullName;
                String repositoryParamType = baseInfo.paramPsiClassFullName;
                String repositoryFuncParamName = baseInfo.getParamFieldName();

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
                String comment = baseInfo.comment;
                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();

                String repositoryReturnClassName = baseInfo.returnPsiClassFullName;
                String repositoryParamClassName = baseInfo.paramPsiClassFullName;

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


                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();
                String dataStoreFieldName = baseInfo.dataStoreFieldName;

                String repositoryReturnClassName = baseInfo.returnPsiClassFullName;
                String repositoryParamClassName = baseInfo.paramPsiClassFullName;


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
                String comment = baseInfo.comment;
                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();

                String repositoryReturnClassName = baseInfo.returnPsiClassFullName;
                String repositoryParamClassName = baseInfo.paramPsiClassFullName;

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
     * 在 DataStore 实现类中 根据选择创建不同的实现
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

    /**
     * 在 DataStore 实现类中 添加方法实现
     * @param project
     * @param mFactory
     * @param dataStoreImplClass
     * @param dataStoreImplInfo
     * @param baseInfo
     */
    private static void toGeneratorDataStoreImplImplCode(Project project, PsiElementFactory mFactory, PsiClass dataStoreImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo) {
        String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
        String paramFieldName = baseInfo.getParamFieldName();
        String dataSourceFieldName = dataStoreImplInfo.generateInterfaceFieldName;

        String repositoryReturnClassName = baseInfo.returnPsiClassFullName;
        String repositoryParamClassName = baseInfo.paramPsiClassFullName;

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

        // 如果需要DataSource 模板代码生成，则根据需要进行生成
        if (dataStoreImplInfo.isNeedGenerate) {
            toGeneratorDataSourceCode(project, mFactory, dataStoreImplClass, dataStoreImplInfo, baseInfo);
            generatorDataSourceImplCode(project, mFactory, dataStoreImplClass, dataStoreImplInfo, baseInfo);
        }
    }

    /**
     * DataSource(例如：CashDB，CashApi) 接口 定义方法
     *
     * @param project
     * @param mFactory
     * @param dataStoreImplClass
     * @param dataStoreImplInfo
     * @param baseInfo
     */
    private static void toGeneratorDataSourceCode(Project project, PsiElementFactory mFactory, PsiClass dataStoreImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo){
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiClass dataSourceInterface = dataStoreImplInfo.generateDataSourceInterface;

                String comment = baseInfo.comment;
                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();

                String repositoryReturnClassName = baseInfo.returnPsiClassFullName;
                String repositoryParamClassName = baseInfo.paramPsiClassFullName;


                if (dataStoreImplInfo.generateType == DataStoreImplInfo.GenerateType.NET) {
                    StringBuilder urlStringBuilder = new StringBuilder();
                    urlStringBuilder.append("/**\n");
                    urlStringBuilder.append(" * " + comment + "\n");
                    urlStringBuilder.append(" */\n");
                    urlStringBuilder.append("String ").append(baseInfo.getApiUrlName()).append(" = com.qianmi.arch.config.Hosts.URL_HOST").append("+\"\";");
                    dataSourceInterface.add(mFactory.createFieldFromText(urlStringBuilder.toString(), dataSourceInterface));
                }


                StringBuilder method = new StringBuilder();
                method.append("/**\n");
                method.append(" * " + comment + "\n");
                method.append(" */\n");
                method.append("io.reactivex.Observable<" + repositoryReturnClassName + "> " + repositoryFuncName + "(");
                if (!baseInfo.isVoidParam()) {
                    method.append(repositoryParamClassName + " " + paramFieldName);
                }
                method.append(");");
                dataSourceInterface.add(mFactory.createMethodFromText(method.toString(), dataSourceInterface));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                styleManager.optimizeImports(dataSourceInterface.getContainingFile());
                styleManager.shortenClassReferences(dataSourceInterface);
                new ReformatCodeProcessor(project, dataSourceInterface.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }

    /**
     * DataSource(例如：CashDB，CashApi) 接口 找到实现类，每个实现类，生成模板方法
     *
     * @param project
     * @param mFactory
     * @param dataStoreImplClass
     * @param dataStoreImplInfo
     * @param baseInfo
     */
    private static void generatorDataSourceImplCode(Project project, PsiElementFactory mFactory, PsiClass dataStoreImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo){
        PsiClass dataSourceInterface = dataStoreImplInfo.generateDataSourceInterface;
        PsiClass[] dataSourceImplClasses = Utils.getImplClasses(dataSourceInterface);
        if (dataSourceImplClasses == null || dataSourceImplClasses.length <= 0) {
            Messages.showMessageDialog(project, "未找到 " + dataStoreImplInfo.generateDataSourceInterface.getName() + " 实现类", "错误", null);
            return;
        }

        for (PsiClass dataSourceImplClass : dataSourceImplClasses) {
            toGeneratorDataSourceImplCode(project, mFactory, dataSourceImplClass, dataStoreImplInfo, baseInfo);
        }
    }

    /**
     * DataSource(例如：CashDB，CashApi) 的实现类 生成对定的模板方法
     *
     * @param project
     * @param mFactory
     * @param dataSourceImplClass
     * @param dataStoreImplInfo
     * @param baseInfo
     */
    private static void toGeneratorDataSourceImplCode(Project project, PsiElementFactory mFactory, PsiClass dataSourceImplClass, DataStoreImplInfo dataStoreImplInfo, BaseInfo baseInfo){
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {


                String repositoryFuncName = baseInfo.getUseCaseActionFuncName();
                String paramFieldName = baseInfo.getParamFieldName();
                String dataStoreFieldName = baseInfo.dataStoreFieldName;

                String repositoryReturnClassName = baseInfo.returnPsiClassFullName;
                String repositoryParamClassName = baseInfo.paramPsiClassFullName;


                StringBuilder method = new StringBuilder();
                method.append("@Override\n");
                method.append("public io.reactivex.Observable<" + repositoryReturnClassName + "> " + repositoryFuncName + "(");
                if (!baseInfo.isVoidParam()) {
                    method.append(repositoryParamClassName + " " + paramFieldName);
                }
                method.append("){");

                // 方法内容
                method.append("  return io.reactivex.Observable.create(emitter -> {\n");

                String templateCode = GenerateTemplateCodeUtils.getTemplateCode(dataSourceImplClass, dataStoreImplInfo, baseInfo);
                if (templateCode != null) {
                    method.append(templateCode);
                }

                method.append("});");
                method.append("}");
                dataSourceImplClass.add(mFactory.createMethodFromText(method.toString(), dataSourceImplClass));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                styleManager.optimizeImports(dataSourceImplClass.getContainingFile());
                styleManager.shortenClassReferences(dataSourceImplClass);
                new ReformatCodeProcessor(project, dataSourceImplClass.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }

}
