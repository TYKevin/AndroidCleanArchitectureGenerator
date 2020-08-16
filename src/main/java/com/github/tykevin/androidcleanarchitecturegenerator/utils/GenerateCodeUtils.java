package com.github.tykevin.androidcleanarchitecturegenerator.utils;

import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

import static com.github.tykevin.androidcleanarchitecturegenerator.utils.ClassNameUtils.subClassNameToFuncName;

public class GenerateCodeUtils {
    public static void generatorUseCaseCode(Project mProject, BaseInfo baseInfo) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(mProject, () -> {
                PsiClass useCaseClass = baseInfo.useCasePsiClass;
                PsiClass repositoryClass = baseInfo.repositoryInterface;
                PsiClass returnClass = baseInfo.returnPsiClass;
                PsiClass paramClass = baseInfo.paramPsiClass;
                String paramFieldName = baseInfo.paramFieldName;

                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(mProject);
                useCaseClass.add(mFactory.createFieldFromText("private final " + repositoryClass.getQualifiedName() + " repository;", useCaseClass));

                StringBuilder method = new StringBuilder();
                method.append("@javax.inject.Inject\n");
                method.append("public " + useCaseClass.getName() + "(com.qianmi.arch.domain.executor.ThreadExecutor threadExecutor, com.qianmi.arch.domain.executor.PostExecutionThread postExecutionThread, " + repositoryClass.getQualifiedName() + " repository) { \n");
                method.append("super(threadExecutor, postExecutionThread);\n");
                method.append("this.repository = repository;}");
                useCaseClass.add(mFactory.createMethodFromText(method.toString(), useCaseClass));

                String repositoryFunc = subClassNameToFuncName(useCaseClass.getName());
                String repositoryReturnType = returnClass.getQualifiedName();
                String repositoryParamType = paramClass.getQualifiedName();
                String repositoryFuncParamName = baseInfo.isNoParam() ? "v" : paramFieldName;
                String repositoryParamName = baseInfo.isNoParam() ? "" : paramFieldName;

                StringBuilder methodBuildUseCase = new StringBuilder();
                methodBuildUseCase.append("@Override\n");
                methodBuildUseCase.append("public io.reactivex.Observable<" + repositoryReturnType + "> buildUseCaseObservable(" + repositoryParamType + "  " + repositoryFuncParamName + ")");
                methodBuildUseCase.append("{\n");
                methodBuildUseCase.append(" return this.repository." + repositoryFunc + "(" + repositoryParamName + ");\n");
                methodBuildUseCase.append("}");
                useCaseClass.add(mFactory.createMethodFromText(methodBuildUseCase.toString(), useCaseClass));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
                styleManager.optimizeImports(useCaseClass.getContainingFile());
                styleManager.shortenClassReferences(useCaseClass);
                new ReformatCodeProcessor(mProject, useCaseClass.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }
}
