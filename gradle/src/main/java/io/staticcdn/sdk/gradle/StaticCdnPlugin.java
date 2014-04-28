package io.staticcdn.sdk.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;

public class StaticCdnPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getExtensions().create("staticcdn", StaticCdnPluginExtension.class);
		project.getExtensions().getExtraProperties().set("OptimiseTask", OptimiseTask.class);
        OptimiseTask optimiseTask = project.getTasks().create("staticcdnOptimise",OptimiseTask.class);
        optimiseTask.setDescription("Optimise static assets with StaticCDN.io");
        optimiseTask.setGroup("build");
        try {
            project.getTasks().getByName("classes").dependsOn(optimiseTask);
        }catch (UnknownTaskException ignore){
        }
    }
}
