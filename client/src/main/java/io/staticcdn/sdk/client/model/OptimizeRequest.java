package io.staticcdn.sdk.client.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptimizeRequest {

    @Deprecated
    private OptimizerOptions optimiserOptions;
    @Deprecated
    private boolean retrieveOptimisedAsText;

    private OptimizerOptions optimizerOptions;
    private Map<String, String> paths = new LinkedHashMap<String, String>();
    private List<String> aggregations;

    public void addPath(String path, String key) {
        paths.put(path, key);
    }

    public OptimizerOptions getOptimizerOptions() {
        if(optimizerOptions==null)
            return optimiserOptions;
        return optimizerOptions;
    }

    public void setOptimizerOptions(OptimizerOptions optimizerOptions) {
        this.optimizerOptions = optimizerOptions;
    }

    public OptimizerOptions getOptimiserOptions() {
        return optimiserOptions;
    }

    public void setOptimiserOptions(OptimizerOptions optimiserOptions) {
        this.optimiserOptions = optimiserOptions;
    }

    public boolean isRetrieveOptimisedAsText() {
        return retrieveOptimisedAsText;
    }

    public void setRetrieveOptimisedAsText(boolean retrieveOptimisedAsText) {
        this.retrieveOptimisedAsText = retrieveOptimisedAsText;
    }

    public Map<String, String> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, String> paths) {
        this.paths = paths;
    }

    public List<String> getAggregations() {
        return aggregations;
    }

    public void setAggregations(List<String> aggregations) {
        this.aggregations = aggregations;
    }
}
