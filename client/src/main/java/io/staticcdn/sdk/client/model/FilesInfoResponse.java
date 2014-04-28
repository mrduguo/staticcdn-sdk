package io.staticcdn.sdk.client.model;

import java.util.Map;

public class FilesInfoResponse {

    private Map<String, FileInfo> files;

    public Map<String, FileInfo> getFiles() {
        return files;
    }

    public void setFiles(Map<String, FileInfo> files) {
        this.files = files;
    }
}
