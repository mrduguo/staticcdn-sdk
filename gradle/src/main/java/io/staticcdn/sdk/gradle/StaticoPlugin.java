package io.staticcdn.sdk.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;

public class StaticoPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("statico", StaticoPluginExtension.class);
        OptimizePluginTask optimizeTask = project.getTasks().create("staticoOptimize", OptimizePluginTask.class);
        optimizeTask.setDescription("Optimize static assets with statico.io");
        optimizeTask.setGroup("build");
        try {
            project.getTasks().getByName("classes").dependsOn(optimizeTask);
        } catch (UnknownTaskException ignore) {
        }
    }
}
