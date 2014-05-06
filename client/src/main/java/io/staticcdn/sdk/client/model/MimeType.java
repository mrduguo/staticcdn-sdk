package io.staticcdn.sdk.client.model;

public class MimeType {
    private String contentType;
    private boolean text;
    private String extension;

    public MimeType() {
    }

    public MimeType(String contentType, boolean text, String extension) {
        this.contentType = contentType;
        this.text = text;
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isText() {
        return text;
    }

    public void setText(boolean text) {
        this.text = text;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String render(){
        if(text)
            return contentType+";charset=UTF-8";
        else
            return contentType;
    }
}
