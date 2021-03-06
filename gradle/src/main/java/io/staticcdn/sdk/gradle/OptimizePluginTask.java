package io.staticcdn.sdk.gradle;

import io.staticcdn.sdk.client.StaticCdnClient;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptimizePluginTask extends DefaultTask {

    @TaskAction
    public void optimize() throws Exception {
        StaticoPluginExtension extension = getConfig();
        if (extension.isSkipOptimize() || System.getProperty("skipOptimize")!=null) {
            getLogger().warn("optimize skipped");
            return;
        }

        List<File> inputWwwRoots = new ArrayList<File>();
        if (extension.getInputWwwRoots() == null) {
            throw new RuntimeException("inputWwwRoots is required");
        } else {
            for (String path : extension.getInputWwwRoots()) {
                File folder;
                if(path.startsWith("/") || path.indexOf(":")>0){
                    folder = new File(path);
                }else{
                    folder = new File(getProject().getProjectDir(), path);
                }
                inputWwwRoots.add(folder);
            }
        }

        File outputWwwRoot = null;
        if (extension.getOutputWwwRoot() != null) {
            if(extension.getOutputWwwRoot().startsWith("/") || extension.getOutputWwwRoot().indexOf(":")>0){
                outputWwwRoot = new File(extension.getOutputWwwRoot());
            }else{
                outputWwwRoot = new File(getProject().getProjectDir(), extension.getOutputWwwRoot());
            }
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
                                File originFile = new File(foundFile.getParentFile(), extension.getOriginalFileNameSuffix() + foundFile.getName());
                                if (!originFile.exists()) {
                                    getLogger().debug("found file: " + foundFile.getAbsolutePath());
                                    inputFileRelativePaths.add(relativePath);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (inputFileRelativePaths.size() == 0) {
            throw new RuntimeException("no file found to optimize");
        }

        StaticCdnClient staticCdnClient = new StaticCdnClient(extension.getApiKey(), extension.getApiSecret());
        for (String filePath : inputFileRelativePaths) {
            staticCdnClient.optimize(
                    inputWwwRoots,
                    outputWwwRoot,
                    filePath,
                    extension.getOptimizerOptions(),
                    extension.getOriginalFileNameSuffix(),
                    extension.getRefsFileNameSuffix()
            );
        }
    }

    protected StaticoPluginExtension getConfig() {
        return getProject().getExtensions().getByType(StaticoPluginExtension.class);
    }
}
