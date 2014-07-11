package io.staticcdn.sdk.client.model;

import java.util.List;
import java.util.Map;

public class OptimizeResponse {

    private String signature;
    private String createdAt;

    private String optimized;

    private List<String> missingKeys;
    private String message;
    private List<String> warnings;
    private Map<String, String> references;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getOptimized() {
        return optimized;
    }

    public void setOptimized(String optimized) {
        this.optimized = optimized;
    }

    public List<String> getMissingKeys() {
        return missingKeys;
    }

    public void setMissingKeys(List<String> missingKeys) {
        this.missingKeys = missingKeys;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Map<String, String> getReferences() {
        return references;
    }

    public void setReferences(Map<String, String> references) {
        this.references = references;
    }
}
