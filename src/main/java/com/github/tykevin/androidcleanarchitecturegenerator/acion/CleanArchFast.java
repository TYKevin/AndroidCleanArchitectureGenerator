package com.github.tykevin.androidcleanarchitecturegenerator.acion;

import com.github.tykevin.androidcleanarchitecturegenerator.beans.BaseInfo;
import com.github.tykevin.androidcleanarchitecturegenerator.form.CleanFastSelector;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.FileUtils;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.GenerateCodeUtils;
import com.github.tykevin.androidcleanarchitecturegenerator.utils.MessageUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CleanArchFast extends AnAction {
    private static final Logger log = Logger.getInstance(CleanArchFast.class);
    private Project project;
    private Editor editor;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        log.setLevel(Level.ALL);
        log.info("===== CleanArchFast Plugin working =======");

        project = e.getData(PlatformDataKeys.PROJECT);
        editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        //如果光标选择的不是类，弹出对话框提醒
        if (psiElement == null || !(psiElement instanceof PsiClass)) {
            MessageUtils.showErrorMsg(project, "请在类名上选择！");
            return;
        }
        // 获取到当前选择类（UseCase）的 PsiClass 对象
        PsiClass useCasePsiClass = (PsiClass) psiElement;

        BaseInfo baseInfo = getBaseInfo(project, editor, useCasePsiClass);
        log.info(baseInfo.toString());


        if (baseInfo.paramPsiClass == null
                || baseInfo.returnPsiClass == null
                || baseInfo.paramPsiClassFullName == null
                || baseInfo.paramPsiClassFullName.length()  == 0
                || baseInfo.returnPsiClassFullName == null
                || baseInfo.returnPsiClassFullName.length()  == 0
        ) {
            MessageUtils.showErrorMsg(project, "出入参准备错误！");
        }

        if (baseInfo.repositoryInterfaceFiles == null) {
            log.error("getRepositoryFileList 错误：repositoryInterfaceFiles == null");
            MessageUtils.showErrorMsg(project, "repositoryInterfaceFiles 准备错误！");
            return;
        }

        // 展示选择框
        CleanFastSelector.showSelectorDialog(project, editor, baseInfo, new CleanFastSelector.ActionListener() {
            @Override
            public void onConfirmAction(BaseInfo info) {
                generateGreatCode(info);
                CleanFastSelector.closeDialog();
            }

            @Override
            public void onCancelAction() {
            }
        });
    }

    private void generateGreatCode(BaseInfo info) {
        GenerateCodeUtils.generatorUseCaseCode(project, info);
        GenerateCodeUtils.generatorRepositoryCode(project, info);
        GenerateCodeUtils.generatorRepositoryImplCode(project, info);
        GenerateCodeUtils.generatorDataStoreCode(project, info);
        GenerateCodeUtils.generatorDataStoreImplCode(project, info);
    }

    private BaseInfo getBaseInfo(Project project, Editor editor, PsiClass useCasePsiClass) {
        BaseInfo baseInfo = new BaseInfo();
        baseInfo.useCasePsiClass = useCasePsiClass;

        // 获取出入参 的 PsiType
        setParamAndReturn(project, useCasePsiClass, baseInfo);

        // domain/repository 下找到 所有的 interface files
        setRepositoryFileList(project, editor, baseInfo);

        return baseInfo;
    }

    /**
     * 设置 domain/repository 下找到 所有的 interface files
     *
     * @param project
     * @param editor
     * @param baseInfo
     */
    private void setRepositoryFileList(Project project, Editor editor, BaseInfo baseInfo) {
        if (baseInfo == null) {
            baseInfo = new BaseInfo();
        }

        PsiFile[] repositoryInterfaceFiles = FileUtils.getRepositoryInterfaces(project, editor);
        baseInfo.repositoryInterfaceFiles = repositoryInterfaceFiles;
    }

    /**
     * 设置出入参
     * @param project
     * @param useCasePsiClass
     * @param baseInfo
     */
    private void setParamAndReturn(Project project, PsiClass useCasePsiClass, BaseInfo baseInfo) {
        if (baseInfo == null) {
            baseInfo = new BaseInfo();
        }

        PsiType returnType = null;
        PsiType paramType = null;
        PsiClassType[]  basePsiClassTypes = useCasePsiClass.getExtendsListTypes();
        if(basePsiClassTypes == null || basePsiClassTypes.length < 1) {
            MessageUtils.showErrorMsg(project, "未检测到对应出参或入参的泛型列表！");
            return;
        }
        PsiClassType base = basePsiClassTypes[0];
        PsiTypeParameter[] psiTypeParameters = base.resolve().getTypeParameters();
        Map<PsiTypeParameter, PsiType> map = base.resolveGenerics().getSubstitutor().getSubstitutionMap();
        if (psiTypeParameters.length >= 2 && map != null) {
            returnType = map.get(psiTypeParameters[0]);
            paramType = map.get(psiTypeParameters[1]);
        }
        if (paramType == null || returnType == null) {
            MessageUtils.showErrorMsg(project, "未检测到对应出参或入参！");
            return;
        }

        log.info("returnType 名：" + returnType.getCanonicalText());
        log.info("paramType 名：" + paramType.getCanonicalText());

        baseInfo.paramPsiClass = PsiTypesUtil.getPsiClass(paramType);
        baseInfo.paramPsiClassFullName = paramType.getCanonicalText();

        baseInfo.returnPsiClass = PsiTypesUtil.getPsiClass(returnType);
        baseInfo.returnPsiClassFullName = returnType.getCanonicalText();
    }


}
