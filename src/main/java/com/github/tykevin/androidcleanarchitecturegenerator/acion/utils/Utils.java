package com.github.tykevin.androidcleanarchitecturegenerator.acion.utils;

import com.intellij.ide.util.PsiClassListCellRenderer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Comparator;

public class Utils {

    private static final Logger log = Logger.getInstance(Utils.class);

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
        log.info("parentDir：" + parentDir);

        PsiDirectory domainDir = parentDir;

        // 找到 Domain 文件夹
        while (domainDir != null && !"domain".equals(domainDir.getName())) {
            // 到 src 层级 截止
            if ("src".equals(domainDir.getName())) {
                return null;
            }

            domainDir = domainDir.getParentDirectory();
            log.info("文件夹 Dir：" + domainDir);
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
                log.info("domain 子文件夹：" + domainSubDir.getName());
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


    public static PsiFile[] getRepositoryListFromCaret(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement candidate = file.findElementAt(offset);

        PsiDirectory parentDir = candidate.getNavigationElement().getContainingFile().getContainingDirectory().getParentDirectory();
        log.info("parentDir：" + parentDir);

        PsiDirectory domainDir = parentDir;

        // 找到 Domain 文件夹
        while (domainDir != null && !"domain".equals(domainDir.getName())) {
            domainDir = domainDir.getParentDirectory();
            log.info("文件夹 Dir：" + domainDir.getParent());
        }

        // 找到 domain/repository 文件夹, 返回
        if (domainDir != null) {
            PsiDirectory[] domainSubDirs = domainDir.getSubdirectories();
            if (domainSubDirs != null) {
                for(PsiDirectory domainSubDir : domainSubDirs) {
                    log.info("domain 子文件夹：" + domainSubDir.getName());
                    if ("repository".equals(domainSubDir.getName())){
                        return domainSubDir.getFiles();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Try to find layout XML file in current source on cursor's position
     *
     * @param editor
     * @param file
     * @return
     */
    public static PsiFile getLayoutFileFromCaret(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();

        PsiElement candidateA = file.findElementAt(offset);
        PsiElement candidateB = file.findElementAt(offset - 1);

        PsiFile layout = findLayoutResource(candidateA);
        if (layout != null) {
            return layout;
        }

        return findLayoutResource(candidateB);
    }

    /**
     * Try to find layout XML file in selected element
     *
     * @param element
     * @return
     */
    public static PsiFile findLayoutResource(PsiElement element) {
        log.info("Finding layout resource for element: " +
                element.getNavigationElement().getContainingFile().getContainingDirectory().getParentDirectory());
        if (element == null) {
            return null; // nothing to be used
        }
        if (!(element instanceof PsiIdentifier)) {
            return null; // nothing to be used
        }

        PsiElement firstLayout = element.getParent().getFirstChild();
        log.info("Finding first layout: " + firstLayout.getText());

        PsiElement lastlayout = element.getParent().getLastChild();
        log.info("Finding last layout: " + lastlayout.getText());

        GlobalSearchScope moduleScope = element.getResolveScope();
        log.info("Finding layout: " + moduleScope.getDisplayName());


        if (firstLayout == null) {
            return null; // no file to process
        }
        if (!"R.layout".equals(firstLayout.getText())) {
            return null; // not layout file
        }

        Project project = element.getProject();
        String name = String.format("%s.xml", element.getText());
        return resolveLayoutResourceFile(element, project, name);
    }

    private static PsiFile resolveLayoutResourceFile(PsiElement element, Project project, String name) {
        // restricting the search to the current module - searching the whole project could return wrong layouts
        Module module = ModuleUtil.findModuleForPsiElement(element);
        PsiFile[] files = null;
        if (module != null) {
            // first omit libraries, it might cause issues like (#103)
            GlobalSearchScope moduleScope = module.getModuleWithDependenciesScope();
            files = FilenameIndex.getFilesByName(project, name, moduleScope);
            if (files == null || files.length <= 0) {
                // now let's do a fallback including the libraries
                moduleScope = module.getModuleWithDependenciesAndLibrariesScope(false);
                files = FilenameIndex.getFilesByName(project, name, moduleScope);
            }
        }
        if (files == null || files.length <= 0) {
            // fallback to search through the whole project
            // useful when the project is not properly configured - when the resource directory is not configured
            files = FilenameIndex.getFilesByName(project, name, new EverythingGlobalScope(project));
            if (files.length <= 0) {
                return null; //no matching files
            }
        }

        // TODO - we have a problem here - we still can have multiple layouts (some coming from a dependency)
        // we need to resolve R class properly and find the proper layout for the R class
        for (PsiFile file : files) {
            log.info("Resolved layout resource file for name [" + name + "]: " + file.getVirtualFile());
        }
        return files[0];
    }
}
