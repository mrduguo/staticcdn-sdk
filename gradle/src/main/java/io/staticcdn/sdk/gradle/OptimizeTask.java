package io.staticcdn.sdk.gradle;

import io.staticcdn.sdk.client.StaticCdnClient;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptimizeTask extends OptimizePluginTask {

    private StaticCdnPluginExtension config;

    public StaticCdnPluginExtension getConfig() {
        return config;
    }

    public void setConfig(StaticCdnPluginExtension config) {
        this.config = config;
    }
}
