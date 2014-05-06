package io.staticcdn.sdk.maven;

import io.staticcdn.sdk.client.StaticCdnClient;
import io.staticcdn.sdk.client.model.OptimiserOptions;
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
 * Optimise files with Static CDN server
 */
@Mojo(name = "optimise", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class OptimiseMojo extends AbstractMojo {

    @Parameter(defaultValue="${project}")
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
     * the file relative to www root path list to be optimised
     */
    @Parameter
    private List<String> inputFileRelativePaths;

    /**
     * scan all input www roots, matched pattern will add to inputFileRelativePaths
     */
    @Parameter
    private List<String> inputFilePathPatterns;

    /**
     * Optimise optimiserOptions
     */
    @Parameter
    private OptimiserOptions optimiserOptions;

    /**
     * get back optimised file as text, by default will detect by file type
     */
    @Parameter(defaultValue = "true")
    private boolean retrieveOptimisedAsText;

    /**
     * add a prefix for the out put file based on original root file name
     */
    @Parameter
    private String optimisedFileNamePrefix;

    /**
     * append suffix for the out put file based on original root file name
     */
    @Parameter
    private String optimisedFileNameSuffix;

    /**
     * the string will be removed from the new output file based on original root file name
     */
    @Parameter(defaultValue = "-origin")
    private String optimisedFileNameRemoveString;

    @Parameter
    private String apiKey;

    @Parameter
    private String apiSecret;

    public void execute() throws MojoExecutionException {
        if (getLog().isDebugEnabled()) {
            Logger rootLogger = Logger.getAnonymousLogger().getParent();
            rootLogger.setLevel(Level.FINE);
            for (Handler handler : rootLogger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    ((ConsoleHandler) handler).setLevel(Level.FINE);
                }
            }
        }

        if(!inputWwwRoots.get(0).exists()){
            inputWwwRoots.remove(0);
            File tempWwwRoot = new File("c:\\http\\" + mavenProject.getProperties().get("deploymentEnvironmentKey") + "\\wwwroot.tmp");
            inputWwwRoots.add(tempWwwRoot);
            getLog().info("FIXME: manually set wwwroot.tmp path to:" + tempWwwRoot.getAbsolutePath());
        }

        if(inputFileRelativePaths==null){
            if(inputFilePathPatterns!=null){
                inputFileRelativePaths=new ArrayList<String>();
            }else{
                throw new MojoExecutionException("you must set inputFileRelativePaths or inputFilePathPattern");
            }
        }
        if(inputFilePathPatterns!=null){
            for (File inputWwwRoot : inputWwwRoots) {
                if(inputWwwRoot.isDirectory()){
                    for (File foundFile : FileUtils.listFiles(inputWwwRoot, null, true)) {
                        String relativePath=foundFile.getAbsolutePath().substring(inputWwwRoot.getAbsolutePath().length());
                        relativePath=relativePath.replaceAll("\\\\","/");
                        for (String filePathPattern : inputFilePathPatterns) {
                            if(relativePath.matches(filePathPattern) && !inputFileRelativePaths.contains(relativePath)){
                                inputFileRelativePaths.add(relativePath);
                            }
                        }
                    }
                }
            }
        }

        if(inputFileRelativePaths.size()==0){
            throw new MojoExecutionException("no file found to optimise");
        }


        StaticCdnClient staticCdnClient = new StaticCdnClient(apiKey,apiSecret);
        for (String filePath : inputFileRelativePaths) {
            try {
                staticCdnClient.optimise(
                        inputWwwRoots,
                        outputWwwRoot,
                        filePath,
                        optimiserOptions,
                        retrieveOptimisedAsText,
                        optimisedFileNamePrefix,
                        optimisedFileNameSuffix,
                        optimisedFileNameRemoveString
                );
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}
