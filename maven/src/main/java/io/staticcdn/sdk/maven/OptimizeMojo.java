package io.staticcdn.sdk.maven;

import io.staticcdn.sdk.client.StaticCdnClient;
import io.staticcdn.sdk.client.model.OptimizerOptions;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimize files with Static CDN server
 */
@Mojo(name = "optimize", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class OptimizeMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject mavenProject;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File buildOutputDirectory;

    /**
     * Location of the file.
     */
    @Parameter(required = true)
    private List<File> inputWwwRoots;

    /**
     * Output www root location, default to first input www root
     */
    @Parameter
    private File outputWwwRoot;

    /**
     * the file relative to www root path list to be optimized
     */
    @Parameter
    private List<String> inputFileRelativePaths;

    /**
     * scan all input www roots, matched pattern will add to inputFileRelativePaths
     */
    @Parameter
    private List<String> inputFilePathPatterns;

    /**
     * Optimize optimizerOptions
     */
    @Parameter
    private OptimizerOptions optimizerOptions;

    /**
     * skip the optimize
     */
    @Parameter(defaultValue = "false", property = "skipOptimize")
    private boolean skipOptimize;

    /**
     * the suffix used to backup original none optimized file, set to skip to not backup the original file
     */
    @Parameter(defaultValue = ".origin")
    private String originalFileNameSuffix;

    /**
     * the suffix used to store the references used in the optimize session, set to skip to not create the file
     */
    @Parameter(defaultValue = ".refs")
    private String refsFileNameSuffix;

    @Parameter
    private String apiKey;

    @Parameter
    private String apiSecret;

    public void execute() throws MojoExecutionException {
        if (skipOptimize) {
            getLog().info("optimize skipped");
            return;
        }
        if (getLog().isDebugEnabled()) {
            Logger rootLogger = Logger.getAnonymousLogger().getParent();
            rootLogger.setLevel(Level.FINE);
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    ((ConsoleHandler) handler).setLevel(Level.FINE);
                }
            }
        }

        if (inputFileRelativePaths == null) {
            if (inputFilePathPatterns != null) {
                inputFileRelativePaths = new ArrayList<String>();
            } else {
                throw new MojoExecutionException("you must set inputFileRelativePaths or inputFilePathPattern");
            }
        }
        if (inputFilePathPatterns != null) {
            for (File inputWwwRoot : inputWwwRoots) {
                if (inputWwwRoot.isDirectory()) {
                    for (File foundFile : FileUtils.listFiles(inputWwwRoot, null, true)) {
                        String relativePath = foundFile.getAbsolutePath().substring(inputWwwRoot.getAbsolutePath().length());
                        relativePath = relativePath.replaceAll("\\\\", "/");
                        for (String filePathPattern : inputFilePathPatterns) {
                            if (relativePath.matches(filePathPattern) && !inputFileRelativePaths.contains(relativePath)) {
                                inputFileRelativePaths.add(relativePath);
                            }
                        }
                    }
                }
            }
        }

        if (inputFileRelativePaths.size() == 0) {
            throw new MojoExecutionException("no file found to optimize");
        }


        StaticCdnClient staticCdnClient = new StaticCdnClient(apiKey, apiSecret);
        for (String filePath : inputFileRelativePaths) {
            try {
                staticCdnClient.optimize(
                        inputWwwRoots,
                        outputWwwRoot,
                        filePath,
                        optimizerOptions,
                        originalFileNameSuffix,
                        refsFileNameSuffix
                );
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}
