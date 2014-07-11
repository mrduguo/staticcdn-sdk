package io.staticcdn.sdk.client.model;

import java.util.List;

public class ServerConfig {

    @Deprecated
    private List<OptimizeScanRule> optimiseScanRules;
    private String version;
    private List<String> apiServerList;
    private List<OptimizeScanRule> optimizeScanRules;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getApiServerList() {
        return apiServerList;
    }

    public void setApiServerList(List<String> apiServerList) {
        this.apiServerList = apiServerList;
    }

    public List<OptimizeScanRule> getOptimizeScanRules() {
        if(optimizeScanRules==null)
            return optimiseScanRules;
        return optimizeScanRules;
    }

    public void setOptimizeScanRules(List<OptimizeScanRule> optimizeScanRules) {
        this.optimizeScanRules = optimizeScanRules;
    }

    public List<OptimizeScanRule> getOptimiseScanRules() {
        return optimiseScanRules;
    }

    public void setOptimiseScanRules(List<OptimizeScanRule> optimiseScanRules) {
        this.optimiseScanRules = optimiseScanRules;
    }
}
