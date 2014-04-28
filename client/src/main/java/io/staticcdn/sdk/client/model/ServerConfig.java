package io.staticcdn.sdk.client.model;

import java.util.List;

public class ServerConfig {

    private String version;
    private List<String> apiServerList;
    private List<OptimiseScanRule> optimiseScanRules;

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

    public List<OptimiseScanRule> getOptimiseScanRules() {
        return optimiseScanRules;
    }

    public void setOptimiseScanRules(List<OptimiseScanRule> optimiseScanRules) {
        this.optimiseScanRules = optimiseScanRules;
    }
}
