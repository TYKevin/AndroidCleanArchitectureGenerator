package com.github.tykevin.androidcleanarchitecturegenerator.acion;

import com.github.tykevin.androidcleanarchitecturegenerator.acion.form.EntryList;
import com.github.tykevin.androidcleanarchitecturegenerator.acion.form.RepostoryFieldSelector;
import com.github.tykevin.androidcleanarchitecturegenerator.acion.form.listener.ICancelListener;
import com.github.tykevin.androidcleanarchitecturegenerator.acion.form.listener.IConfirmListener;
import com.github.tykevin.androidcleanarchitecturegenerator.acion.utils.Utils;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.lang.jvm.JvmClassKind;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.log4j.Level;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CleanClearFast extends AnAction {

    private static final Logger log = Logger.getInstance(CleanClearFast.class);
    private JFrame mDialog;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        log.setLevel(Level.ALL);
        log.info("开始啦！！！");

        // 1. 在 UseCase 中找到方法 buildUseCaseObservable，并识别 入参 和 出参
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);

        PsiElement psiElement = e.getData(LangDataKeys.PSI_ELEMENT);
        //如果光标选择的不是类，弹出对话框提醒
        if (psiElement == null || !(psiElement instanceof PsiClass)) {
            Messages.showMessageDialog(project, "Please focus on a class", "Generate Failed", null);
            return;
        }

        PsiClass psiClass = (PsiClass) psiElement;

        // 获取 class 泛型中的 类
        PsiType returnType = null;
        PsiType paramType = null;
        PsiClassType base = psiClass.getExtendsListTypes()[0];
        Map<PsiTypeParameter, PsiType> map = base.resolveGenerics().getSubstitutor().getSubstitutionMap();
        PsiTypeParameter[] psiTypeParameters = base.resolve().getTypeParameters();
        if (psiTypeParameters.length >= 2 && map != null) {
            returnType = map.get(psiTypeParameters[0]);
            paramType = map.get(psiTypeParameters[1]);
        }

        if (paramType != null && returnType != null) {
            log.info("returnType 名：" + returnType.getCanonicalText());
            log.info("paramType 名：" + paramType.getCanonicalText());
        }

        // 2. domain/repository 下找到 所有的 interface（repository），并提供【选择,(添加注释）】

        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        // 找到 domain
        PsiDirectory domainDir = Utils.getDomainDir(editor, file);
        if (domainDir == null) {
            Messages.showMessageDialog(project, "未找到 domain 文件夹", "错误", null);
            return;
        }

        // 获取 domain/repository 下所有的文件
        PsiFile[] repositoryFiles = Utils.getRepositoryFiles(domainDir);
        if (domainDir == null) {
            Messages.showMessageDialog(project, "未找到 repository 文件夹", "错误", null);
            return;
        }
        // 弹出选择框
        showSelectDialog(project, editor, psiClass, returnType, paramType, repositoryFiles);
    }

    private void showSelectDialog(Project project, Editor editor, PsiClass psiClass, PsiType returnType, PsiType paramType, PsiFile[] repositoryFiles) {
        // 弹出选择框，提供选择
        EntryList panel = new EntryList(project, editor, new ArrayList<>(Arrays.asList(repositoryFiles)),
                new IConfirmListener() {
                    @Override
                    public void onConfirm(Project mProject, Editor mEditor, ArrayList<PsiFile> mElements, PsiFile psiFile) {
                        if (psiFile == null) {
                            return;
                        }
                        closeDialog();

                        // 获取选择的 repository 的 PsiClass 对象
                        PsiClass repositoryClass = PsiTreeUtil.findChildOfAnyType(psiFile.getOriginalElement(), PsiClass.class);

                        String command = "注释"; // todo: 可以添加注释 以及 returnType 和 paramType 的注释

                        // 3. 根据选择生成对应 repository 的对象，生成构造方法 和 buildUseCaseObservable 方法
                        generatorUseCaseCode(mProject, repositoryClass, project, psiClass, returnType, paramType);
                        // 4. 在 Repository 接口中，定义方法，加上注释, 并在实现类中实现方法
                        ApplicationManager.getApplication().invokeLater(() -> {
                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(project);
                                String repositoryReturnType = returnType != null ? returnType.getCanonicalText() : "";
                                String repositoryParamType = paramType != null ? paramType.getCanonicalText() : "";

                                StringBuilder method = new StringBuilder();
                                method.append("/**\n");
                                method.append(" * " + command + "\n");
                                method.append(" */\n");
                                method.append("Observable<" + repositoryReturnType + "> " + classNameToFuncName(psiClass.getName()) + "(" + repositoryParamType + " " + subClassNameToFuncName(repositoryParamType) + ");");
                                // todo: 生成方法前先判断是否有此方法
                                repositoryClass.add(mFactory.createMethodFromText(method.toString(), repositoryClass));


                                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
                                styleManager.optimizeImports(repositoryClass.getContainingFile());
                                styleManager.shortenClassReferences(repositoryClass);
                                new ReformatCodeProcessor(mProject, repositoryClass.getContainingFile(), null, false).runWithoutProgress();
                            });
                        });

                        // 5. 在 Repository 实现类中，寻找 type 为 interface 的 DataStore变量，【提供选择（在线，离线，在线/离线）】
                        // 5.1 只有 1个 DataStore 实例
                        // 5.2 没有 DataStore，只有 Factory的 实例
                        // 5.3 有 好几个 DataStore 实例
                        // 查找 repostory 的实现类
                        PsiClass[] repostoryImplClasses = Utils.getImplClasses(repositoryClass);
                        for (PsiClass implClass : repostoryImplClasses) {
                            log.info("implClass = " + implClass.getQualifiedName());
                        }

                        if (repostoryImplClasses == null || repostoryImplClasses.length <= 0) {
                            Messages.showMessageDialog(project, "未找到 repostory 实现类", "错误", null);
                            return;
                        }

                        PsiClass repostoryImplClass = repostoryImplClasses[0];
                        PsiField[] repostoryFields = repostoryImplClass.getFields();
                        if (repostoryFields == null || repostoryFields.length <= 0) {
                            Messages.showMessageDialog(project, "repostory 实现类中未找到 DataStore Fields", "错误", null);
                            return;
                        }

                        Map<String, PsiClass> repostoryFieldMap = new LinkedHashMap<>();
                        for (PsiField repostoryImplFields : repostoryFields) {
                            log.info("repostoryImplFields = " + repostoryImplFields.getName());
                            PsiClass fieldClass = PsiTypesUtil.getPsiClass(repostoryImplFields.getType());
                            if (fieldClass == null) {
                                continue;
                            }

                            log.info("repostoryImplFields class: " + fieldClass.getQualifiedName());
                            log.info("repostoryImplFields class kind: " + fieldClass.getClassKind());

                            String fieldName = repostoryImplFields.getName();
                            String fieldClassName = fieldClass.getName();
                            JvmClassKind fieldClassKind = fieldClass.getClassKind();

                            boolean isDataStoreRef = (fieldClassName.toLowerCase().contains("factory") && fieldClassKind == JvmClassKind.CLASS)
                                    || fieldClassKind == JvmClassKind.INTERFACE;

                            if (isDataStoreRef) {
                                repostoryFieldMap.put(fieldName, fieldClass);
                            }
                        }

                        // 根据生成的 field map 弹出选择框
                        showRepostoryFieldSelector(repostoryFieldMap);


                        // 在 Repostory 中生成方法，并根据上述选择，填充方法主体

                        // 6. 去 DataStore 中定义接口，并实现在子类中,【todo：此处需要选择返回的Bean，是否与 Repository 层相同】
                        // 7. 在 DataStoreNetImpl 中， 找到 Api interface，定义接口方法，生成服务器接口链接，并将方法实现添加在子类中
                        // 8. 在 DataStoreNetImpl 中 添加调用代码，【选择 Get/Post】
                        // 9. 在 Api 实现方法中 添加实现网络调用代码
            /*
             *  return Observable.create(emitter -> {
            try
            {
                String response = requestFromApi(MODIFY_ORDER_INFO, GsonHelper.toJson(request));
                if (response != null)
                {
                    BaseResponseEntity baseResponseEntity = GsonHelper.toType(response, BaseResponseEntity.class);
                    assert baseResponseEntity != null;
                    if (GeneralUtils.isNotNullOrZeroLength(baseResponseEntity.status)
                            && baseResponseEntity.status.equals(Config.RESPONSE_SUCCESS)
                    )
                    {
                        emitter.onNext(true);
                        emitter.onComplete();
                    }
                    else
                    {
                        emitter.onError(new DefaultErrorBundle(baseResponseEntity.status, baseResponseEntity.message));
                    }
                }
                else
                {
                    emitter.onError(new DefaultErrorBundle());
                }
            } catch (Exception e)
            {
                emitter.onError(new DefaultErrorBundle());
            }
        });
             */
                    }
                }, new ICancelListener() {
            @Override
            public void onCancel() {
                closeDialog();
            }
        });

        mDialog = new JFrame();
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
        mDialog.setTitle("选择 repository 类");
        mDialog.getContentPane().add(panel);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    /**
     * 根据 Map 弹出选择框
     *
     * 选择规则：
     * constants("Factory") 且 kind 为CLASS，则为  factory
     *
     * INTERFACE 为生成好的 DataStore 对象
     *
     * 生成逻辑：
     * 选择 DataStoreFactory
     *      单选 createCashDataStore/ createNetCashDataStore/ createCacheCashDataStore方法，并生成对应的DataStore 对象
     * （或者）单选 DataStore 对象
     *      直接使用
     */
    private void showRepostoryFieldSelector(Map<String, PsiClass> repostoryFieldMap) {
        log.info("map = " + repostoryFieldMap);
        RepostoryFieldSelector panel = new RepostoryFieldSelector(repostoryFieldMap, null, null);
        mDialog = new JFrame();
        mDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mDialog.getRootPane().setDefaultButton(panel.getConfirmButton());
        mDialog.setTitle("选择 repository 类");
        mDialog.getContentPane().add(panel);
        mDialog.pack();
        mDialog.setLocationRelativeTo(null);
        mDialog.setVisible(true);
    }

    private void generatorUseCaseCode(Project mProject, PsiClass repositoryClass, Project project, PsiClass psiClass, PsiType returnType, PsiType paramType) {
        ApplicationManager.getApplication().invokeLater(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiElementFactory mFactory = JavaPsiFacade.getElementFactory(project);

                psiClass.add(mFactory.createFieldFromText("private final " + repositoryClass.getQualifiedName() + " repository;", psiClass));

                StringBuilder method = new StringBuilder();
                method.append("@javax.inject.Inject\n");
                method.append("public " + psiClass.getName() + "(com.qianmi.arch.domain.executor.ThreadExecutor threadExecutor, com.qianmi.arch.domain.executor.PostExecutionThread postExecutionThread, " + repositoryClass.getQualifiedName() + " repository) { \n");
                method.append("super(threadExecutor, postExecutionThread);\n");
                method.append("this.repository = repository;}");
                psiClass.add(mFactory.createMethodFromText(method.toString(), psiClass));

                String repositoryFunc = classNameToFuncName(psiClass.getName());
                String repositoryReturnType = returnType != null ? returnType.getCanonicalText() : "";
                String repositoryParamType = paramType != null ? paramType.getCanonicalText() : "";
                String repositoryFuncParam = "Void".equals(repositoryParamType) ? "" : subClassNameToFuncName(repositoryParamType);
                String repositoryFuncParamName = subClassNameToFuncName(repositoryParamType);// todo: 支持自定义参数名称

                StringBuilder methodBuildUseCase = new StringBuilder();
                methodBuildUseCase.append("@Override\n");
                methodBuildUseCase.append("public io.reactivex.Observable<" + repositoryReturnType + "> buildUseCaseObservable(" + repositoryParamType + "  " + repositoryFuncParamName + ")");
                methodBuildUseCase.append("{\n");
                methodBuildUseCase.append(" return this.repository." + repositoryFunc + "(" + repositoryFuncParam + ");\n");
                methodBuildUseCase.append("}");
                psiClass.add(mFactory.createMethodFromText(methodBuildUseCase.toString(), psiClass));

                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mProject);
                styleManager.optimizeImports(psiClass.getContainingFile());
                styleManager.shortenClassReferences(psiClass);
                new ReformatCodeProcessor(mProject, psiClass.getContainingFile(), null, false).runWithoutProgress();
            });
        });
    }

    protected void closeDialog() {
        if (mDialog == null) {
            return;
        }

        mDialog.setVisible(false);
        mDialog.dispose();
    }

    /**
     * 切换类名（全路径）到方法名
     *
     * @param className
     * @return
     */
    public String subClassNameToFuncName(String className) {
        if (className == null || className.length() <= 1) {
            return "";
        }

        // 不是全称，或者.是最后一个
        if (className.indexOf(".") <= 0 || className.lastIndexOf(".") == className.length() - 1) {
            return classNameToFuncName(className);
        }

        // 从全类名中截取类名
        className = className.substring(className.lastIndexOf(".") + 1, className.length());
        return classNameToFuncName(className);
    }

    /**
     * 切换类名（SimpleName）到方法名
     *
     * @param className
     * @return
     */
    public String classNameToFuncName(String className) {
        if (className == null || className.length() <= 1) {
            return "";
        }

        return (change(className.charAt(0))) + className.substring(1, className.length());
    }

    /**
     * 大小写转换
     *
     * @param c
     * @return
     */
    private static char change(char c) {
        //如果输入的是大写，+32即可得到小写
        if (c >= 'A' && c <= 'Z') {
            return c += 32;
        } else if (c >= 'a' && c <= 'z') {    //如果输入的是小写，-32即可得大小写
            return c -= 32;
        } else {
            return c;
        }
    }
}
