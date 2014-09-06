package io.staticcdn.sdk.client.model;

public class OptimizerOptions {

    private Integer version;
    private String profile;
    private String cdnBaseUrl;
    private Boolean autoEmbedCss;
    private Integer autoDataUrlMaxFileSize;
    private Integer autoSpriteMinFileSize;
    private Integer autoSpriteMaxFileSize;
    private String cleancssOptions;
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

    public String getUglifyjsOptions() {
        return uglifyjsOptions;
    }

    public void setUglifyjsOptions(String uglifyjsOptions) {
        this.uglifyjsOptions = uglifyjsOptions;
    }

    public String getLessOptions() {
        return lessOptions;
    }

    public void setLessOptions(String lessOptions) {
        this.lessOptions = lessOptions;
    }

    public String getCleancssOptions() {
        return cleancssOptions;
    }

    public void setCleancssOptions(String cleancssOptions) {
        this.cleancssOptions = cleancssOptions;
    }
}
