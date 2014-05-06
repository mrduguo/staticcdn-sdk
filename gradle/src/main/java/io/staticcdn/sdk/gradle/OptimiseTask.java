
package io.staticcdn.sdk.gradle;

import io.staticcdn.sdk.client.StaticCdnClient;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptimiseTask extends DefaultTask {

    @TaskAction
    public void optimise() throws Exception {
        StaticCdnPluginExtension extension = getProject().getExtensions().getByType(StaticCdnPluginExtension.class);

        List<File> inputWwwRoots = new ArrayList<File>();
        if (extension.getInputWwwRoots() == null) {
            throw new RuntimeException("inputWwwRoots is required");
        } else {
            for (String path : extension.getInputWwwRoots()) {
                inputWwwRoots.add(new File(path));
            }
        }

        File outputWwwRoot = null;
        if (extension.getOutputWwwRoot() != null) {
            outputWwwRoot = new File(extension.getOutputWwwRoot());
        }

        List<String> inputFileRelativePaths = extension.getInputFileRelativePaths();
        if (inputFileRelativePaths == null) {
            if (extension.getInputFilePathPatterns() != null) {
                inputFileRelativePaths = new ArrayList<String>();
            } else {
                throw new RuntimeException("you must set inputFileRelativePaths or inputFilePathPatterns");
            }
        }
        if (extension.getInputFilePathPatterns() != null) {
            for (File inputWwwRoot : inputWwwRoots) {
                if (inputWwwRoot.isDirectory()) {
                    for (File foundFile : FileUtils.listFiles(inputWwwRoot, null, true)) {
                        String relativePath = foundFile.getAbsolutePath().substring(inputWwwRoot.getAbsolutePath().length());
                        relativePath = relativePath.replaceAll("\\\\", "/");
                        for (String filePathPattern : extension.getInputFilePathPatterns()) {
                            if (relativePath.matches(filePathPattern) && !inputFileRelativePaths.contains(relativePath)) {
                                inputFileRelativePaths.add(relativePath);
                            }
                        }
                    }
                }
            }
        }

        if(inputFileRelativePaths.size()==0){
            throw new RuntimeException("no file found to optimise");
        }

        StaticCdnClient staticCdnClient = new StaticCdnClient(extension.getApiKey(),extension.getApiSecret());
        for (String filePath : inputFileRelativePaths) {
            staticCdnClient.optimise(
                    inputWwwRoots,
                    outputWwwRoot,
                    filePath,
                    extension.getOptimiserOptions(),
                    extension.isRetrieveOptimisedAsText(),
                    extension.getOptimisedFileNamePrefix(),
                    extension.getOptimisedFileNameSuffix(),
                    extension.getOptimisedFileNameRemoveString()
            );
        }
    }
}
