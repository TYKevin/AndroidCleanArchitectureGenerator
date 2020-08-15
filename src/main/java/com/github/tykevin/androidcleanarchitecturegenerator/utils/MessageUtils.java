package com.github.tykevin.androidcleanarchitecturegenerator.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;

public class MessageUtils {


    public static void showErrorMsg(Project project, String msg) {
        showMsg(project, msg, "错误" , null);
    }

    public static void showMsg(Project project, String msg, String title, Icon icon) {
        Messages.showMessageDialog(project, msg, title , icon);
    }
}
