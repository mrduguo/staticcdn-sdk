package io.staticcdn.sdk.gradle;

public class OptimizeTask extends OptimizePluginTask {

    private StaticoPluginExtension config;

    public StaticoPluginExtension getConfig() {
        return config;
    }

    public void setConfig(StaticoPluginExtension config) {
        this.config = config;
    }
}
