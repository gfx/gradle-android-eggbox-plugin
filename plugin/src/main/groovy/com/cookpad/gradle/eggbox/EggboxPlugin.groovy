package com.cookpad.gradle.eggbox

import org.gradle.api.Plugin
import org.gradle.api.Project

// see http://www.gradle.org/docs/current/userguide/custom_plugins.html

public class EggboxPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.add(EggboxExtension.NAME, EggboxExtension as Object)

        project.task(EggboxTask, type: EggboxTask)
    }
}
