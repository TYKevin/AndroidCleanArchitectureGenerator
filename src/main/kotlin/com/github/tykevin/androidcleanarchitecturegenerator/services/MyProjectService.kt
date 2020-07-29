package com.github.tykevin.androidcleanarchitecturegenerator.services

import com.intellij.openapi.project.Project
import com.github.tykevin.androidcleanarchitecturegenerator.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
