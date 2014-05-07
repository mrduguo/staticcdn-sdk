package io.staticcdn.sdk.client.model;

public class OptimiserOptions {

    private String profile;
    private Boolean autoEmbedCss;
    private Integer autoDataUrlMaxFileSize;
    private Integer autoSpriteMinFileSize;
    private Integer autoSpriteMaxFileSize;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
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
}
