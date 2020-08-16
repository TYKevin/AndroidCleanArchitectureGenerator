package com.github.tykevin.androidcleanarchitecturegenerator.utils;

import com.intellij.ide.util.PsiClassListCellRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.PsiElementProcessorAdapter;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.util.PsiUtilBase;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public class FileUtils {
    private static final Logger log = Logger.getInstance(FileUtils.class);

    /**
     * 获取当前 UseCase
     * @param project
     * @param editor
     * @return
     */
    @Nullable
    public static PsiFile[] getRepositoryInterfaces(Project project, Editor editor) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        // 找到 domain
        PsiDirectory domainDir = FileUtils.getDomainDir(editor, file);
        if (domainDir == null) {
            MessageUtils.showErrorMsg(project, "未找到 domain 文件夹");
            return null;
        }

        // 获取 domain/repository 下所有的文件
        PsiFile[] repositoryInterfaceFiles = FileUtils.getRepositoryFiles(domainDir);
        if (domainDir == null) {
            MessageUtils.showErrorMsg(project, "未找到 repository 文件夹");
            return null;
        }
        return repositoryInterfaceFiles;
    }

    /**
     * 根据选择的节点，找到 Domain 文件夹
     * @param editor
     * @param file
     * @return
     */
    public static PsiDirectory getDomainDir(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement candidate = file.findElementAt(offset);

        PsiDirectory parentDir = candidate.getNavigationElement().getContainingFile().getContainingDirectory().getParentDirectory();
        log.info("UseCase parentDir：" + parentDir);

        PsiDirectory domainDir = parentDir;

        // 找到 Domain 文件夹
        while (domainDir != null && !"domain".equals(domainDir.getName())) {
            // 到 src 层级 截止
            if ("src".equals(domainDir.getName())) {
                return null;
            }

            domainDir = domainDir.getParentDirectory();
        }

        return domainDir;
    }

    /**
     * 找到 domain/repository 文件夹下的 所有repository文件
     * @param domainDir
     * @return
     */
    public static PsiFile[] getRepositoryFiles(PsiDirectory domainDir) {
        if(domainDir == null) {
            return null;
        }

        PsiDirectory[] domainSubDirs = domainDir.getSubdirectories();
        if (domainSubDirs != null) {
            for(PsiDirectory domainSubDir : domainSubDirs) {
                log.info("扫描 domain 子文件夹：" + domainSubDir.getName());
                if ("repository".equals(domainSubDir.getName())){
                    return domainSubDir.getFiles();
                }
            }
        }
        return null;
    }

    /**
     * 查找实现类
     * @param repositoryClass
     * @return
     */
    public static PsiClass[] getImplClasses(PsiClass repositoryClass) {
        PsiElementProcessor.CollectElementsWithLimit<PsiClass> processor = new PsiElementProcessor.CollectElementsWithLimit<>(5, new THashSet<>());
        ClassInheritorsSearch.search(repositoryClass).forEach(new PsiElementProcessorAdapter<>(processor));

        if (processor.isOverflow()) {
            return null;
        }

        PsiClass[] subclasses = processor.toArray(PsiClass.EMPTY_ARRAY);
        if (subclasses.length == 0) {
            return null;
        }

        Comparator<PsiClass> comparator = new PsiClassListCellRenderer().getComparator();
        Arrays.sort(subclasses, comparator);

        return subclasses;
    }



}
