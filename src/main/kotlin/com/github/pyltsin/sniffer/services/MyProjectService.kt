package com.github.pyltsin.sniffer.services

import com.github.pyltsin.sniffer.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
