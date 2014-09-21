package io.staticcdn.sdk.gradle;

import io.staticcdn.sdk.client.model.OptimizerOptions;

import java.util.List;


public class StaticoPluginExtension {


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
     * the suffix used to backup original none optimized file, set to skip to not backup the original file
     */
    private String originalFileNameSuffix = ".origin";



    /**
     * the suffix used to store the references used in the optimize session, set to skip to not create the file
     */
    private String refsFileNameSuffix = ".refs";

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

    public String getOriginalFileNameSuffix() {
        return originalFileNameSuffix;
    }

    public void setOriginalFileNameSuffix(String originalFileNameSuffix) {
        this.originalFileNameSuffix = originalFileNameSuffix;
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

    public String getRefsFileNameSuffix() {
        return refsFileNameSuffix;
    }

    public void setRefsFileNameSuffix(String refsFileNameSuffix) {
        this.refsFileNameSuffix = refsFileNameSuffix;
    }
}
