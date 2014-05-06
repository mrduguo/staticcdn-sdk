package io.staticcdn.sdk.client.model;

import java.util.List;

public class OptimiseResponse {

    private String signature;
    private String createdAt;

    private String optimised;

    private List<String> missingKeys;
    private String message;
    private List<String> warnings;

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

    public String getOptimised() {
        return optimised;
    }

    public void setOptimised(String optimised) {
        this.optimised = optimised;
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
}
