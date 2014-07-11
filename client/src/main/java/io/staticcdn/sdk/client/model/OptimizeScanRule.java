package io.staticcdn.sdk.client.model;

public class OptimizeScanRule {
    private String extensionPattern;
    private String urlPattern;
    private int urlGroupIndex;

    public String getExtensionPattern() {
        return extensionPattern;
    }

    public void setExtensionPattern(String extensionPattern) {
        this.extensionPattern = extensionPattern;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public int getUrlGroupIndex() {
        return urlGroupIndex;
    }

    public void setUrlGroupIndex(int urlGroupIndex) {
        this.urlGroupIndex = urlGroupIndex;
    }
}
