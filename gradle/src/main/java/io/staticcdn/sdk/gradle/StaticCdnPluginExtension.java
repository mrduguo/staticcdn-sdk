
package io.staticcdn.sdk.gradle;

import io.staticcdn.sdk.client.model.OptimiserOptions;

import java.util.List;

/**
 * Gradle DSL Extension for 'Spring Boot'.  Most of the time Spring Boot can guess the
 * settings in this extension, but occasionally you might need to explicitly set one
 * or two of them. E.g.
 * 
 * <pre>
 *     apply plugin: "spring-boot"
 *     springBoot {
 *         mainClass = 'org.demo.Application'
 *         layout = 'ZIP'
 *     }
 * </pre>
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
public class StaticCdnPluginExtension {


    /**
     * Location of the file.
     */
    private List<String> inputWwwRoots;

    /**
     * Output www root location, default to first input www root
     */
    private String outputWwwRoot;

    /**
     * the file relative to www root path list to be optimised
     */
    private List<String> inputFileRelativePaths;

    /**
     * scan all input www roots, matched pattern will add to inputFileRelativePaths
     */
    private List<String> inputFilePathPatterns;

    /**
     * Optimise optimiserOptions
     */
    private OptimiserOptions optimiserOptions;

    /**
     * get back optimised file as text, by default will detect by file type
     */
    private boolean retrieveOptimisedAsText=true;

    /**
     * the string will be removed from the new output file based on original root file name
     */
    private String optimisedFileNamePrefix="origin-";



    private String apiKey;

    private String apiSecret;

    public List<String> getInputWwwRoots() {
        return inputWwwRoots;
    }

    public void setInputWwwRoots(List<String> inputWwwRoots) {
        this.inputWwwRoots = inputWwwRoots;
    }

    public String getOutputWwwRoot() {
        return outputWwwRoot;
    }

    public void setOutputWwwRoot(String outputWwwRoot) {
        this.outputWwwRoot = outputWwwRoot;
    }

    public List<String> getInputFileRelativePaths() {
        return inputFileRelativePaths;
    }

    public void setInputFileRelativePaths(List<String> inputFileRelativePaths) {
        this.inputFileRelativePaths = inputFileRelativePaths;
    }

    public List<String> getInputFilePathPatterns() {
        return inputFilePathPatterns;
    }

    public void setInputFilePathPatterns(List<String> inputFilePathPatterns) {
        this.inputFilePathPatterns = inputFilePathPatterns;
    }

    public OptimiserOptions getOptimiserOptions() {
        return optimiserOptions;
    }

    public void setOptimiserOptions(OptimiserOptions optimiserOptions) {
        this.optimiserOptions = optimiserOptions;
    }

    public boolean isRetrieveOptimisedAsText() {
        return retrieveOptimisedAsText;
    }

    public void setRetrieveOptimisedAsText(boolean retrieveOptimisedAsText) {
        this.retrieveOptimisedAsText = retrieveOptimisedAsText;
    }

    public String getOptimisedFileNamePrefix() {
        return optimisedFileNamePrefix;
    }

    public void setOptimisedFileNamePrefix(String optimisedFileNamePrefix) {
        this.optimisedFileNamePrefix = optimisedFileNamePrefix;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
}
