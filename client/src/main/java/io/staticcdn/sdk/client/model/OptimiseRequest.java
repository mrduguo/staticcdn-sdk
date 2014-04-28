package io.staticcdn.sdk.client.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptimiseRequest {

    private OptimiserOptions optimiserOptions;
    private boolean retrieveOptimisedAsText;
    private Map<String, String> paths = new LinkedHashMap<String, String>();
    private List<String> aggregations;

    public void addPath(String path, String key) {
        paths.put(path, key);
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
