package io.staticcdn.sdk.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;

public class StaticCdnPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("staticcdn", StaticCdnPluginExtension.class);
        project.getExtensions().getExtraProperties().set("OptimizeTask", OptimizeTask.class);
        OptimizePluginTask optimizeTask = project.getTasks().create("staticcdnOptimize", OptimizePluginTask.class);
        optimizeTask.setDescription("Optimize static assets with statico.io");
        optimizeTask.setGroup("build");
        try {
            project.getTasks().getByName("classes").dependsOn(optimizeTask);
        } catch (UnknownTaskException ignore) {
        }
    }
}
