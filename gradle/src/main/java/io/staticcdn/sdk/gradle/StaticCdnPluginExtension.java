package io.staticcdn.sdk.gradle;

import io.staticcdn.sdk.client.model.OptimizerOptions;

import java.util.List;

/**
 * Gradle DSL Extension for 'Spring Boot'.  Most of the time Spring Boot can guess the
 * settings in this extension, but occasionally you might need to explicitly set one
 * or two of them. E.g.
 * <p/>
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
     * the file relative to www root path list to be optimized
     */
    private List<String> inputFileRelativePaths;

    /**
     * scan all input www roots, matched pattern will add to inputFileRelativePaths
     */
    private List<String> inputFilePathPatterns;

    /**
     * Optimize optimizerOptions
     */
    private OptimizerOptions optimizerOptions;

    /**
     * the string will be removed from the new output file based on original root file name, set to skip to not backup the original file
     */
    private String optimizedFileNamePrefix = "origin-";

    private boolean skipOptimize = false;


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

    public OptimizerOptions getOptimizerOptions() {
        return optimizerOptions;
    }

    public void setOptimizerOptions(OptimizerOptions optimizerOptions) {
        this.optimizerOptions = optimizerOptions;
    }

    public String getOptimizedFileNamePrefix() {
        return optimizedFileNamePrefix;
    }

    public void setOptimizedFileNamePrefix(String optimizedFileNamePrefix) {
        this.optimizedFileNamePrefix = optimizedFileNamePrefix;
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

    public boolean isSkipOptimize() {
        return skipOptimize;
    }

    public void setSkipOptimize(boolean skipOptimize) {
        this.skipOptimize = skipOptimize;
    }
}
