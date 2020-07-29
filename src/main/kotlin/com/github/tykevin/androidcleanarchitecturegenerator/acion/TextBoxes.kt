package com.github.tykevin.androidcleanarchitecturegenerator.acion

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.ui.Messages

class TextBoxes : AnAction("Text _Boxes") {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(PlatformDataKeys.PROJECT)//这个Project是不是很熟悉？它其实跟Gradle中的Project意义是一样的
        val txt = Messages.showInputDialog(project, "What is your name?", "Input your name", Messages.getQuestionIcon())
        Messages.showMessageDialog(project, "Hello, $txt!\\n I am glad to see you.", "Information", Messages.getInformationIcon())//这两段代码其实很简单，都不用解释了
    }
}