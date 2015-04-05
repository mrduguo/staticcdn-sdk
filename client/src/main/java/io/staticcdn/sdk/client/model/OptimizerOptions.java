package io.staticcdn.sdk.client.model;

public class OptimizerOptions {

    private Integer version;
    private String profile;
    private String cdnBaseUrl;
    private Boolean downloadRefs;

    private Boolean autoEmbedCss;
    private Integer autoDataUrlMaxFileSize;
    private Integer autoSpriteMinFileSize;
    private Integer autoSpriteMaxFileSize;
    private Boolean autoEmbedJs;

    private String cleancssOptions;
    private String uncssOptions;
    private String lessOptions;
    private String uglifyjsOptions;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCdnBaseUrl() {
        return cdnBaseUrl;
    }

    public void setCdnBaseUrl(String cdnBaseUrl) {
        this.cdnBaseUrl = cdnBaseUrl;
    }

    public Boolean getDownloadRefs() {
        return downloadRefs;
    }

    public void setDownloadRefs(Boolean downloadRefs) {
        this.downloadRefs = downloadRefs;
    }

    public Boolean getAutoEmbedCss() {
        return autoEmbedCss;
    }

    public void setAutoEmbedCss(Boolean autoEmbedCss) {
        this.autoEmbedCss = autoEmbedCss;
    }

    public Integer getAutoDataUrlMaxFileSize() {
        return autoDataUrlMaxFileSize;
    }

    public void setAutoDataUrlMaxFileSize(Integer autoDataUrlMaxFileSize) {
        this.autoDataUrlMaxFileSize = autoDataUrlMaxFileSize;
    }

    public Integer getAutoSpriteMinFileSize() {
        return autoSpriteMinFileSize;
    }

    public void setAutoSpriteMinFileSize(Integer autoSpriteMinFileSize) {
        this.autoSpriteMinFileSize = autoSpriteMinFileSize;
    }

    public Integer getAutoSpriteMaxFileSize() {
        return autoSpriteMaxFileSize;
    }

    public void setAutoSpriteMaxFileSize(Integer autoSpriteMaxFileSize) {
        this.autoSpriteMaxFileSize = autoSpriteMaxFileSize;
    }

    public Boolean getAutoEmbedJs() {
        return autoEmbedJs;
    }

    public void setAutoEmbedJs(Boolean autoEmbedJs) {
        this.autoEmbedJs = autoEmbedJs;
    }

    public String getCleancssOptions() {
        return cleancssOptions;
    }

    public void setCleancssOptions(String cleancssOptions) {
        this.cleancssOptions = cleancssOptions;
    }

    public String getUncssOptions() {
        return uncssOptions;
    }

    public void setUncssOptions(String uncssOptions) {
        this.uncssOptions = uncssOptions;
    }

    public String getLessOptions() {
        return lessOptions;
    }

    public void setLessOptions(String lessOptions) {
        this.lessOptions = lessOptions;
    }

    public String getUglifyjsOptions() {
        return uglifyjsOptions;
    }

    public void setUglifyjsOptions(String uglifyjsOptions) {
        this.uglifyjsOptions = uglifyjsOptions;
    }
}
