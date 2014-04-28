package io.staticcdn.sdk.client.model;

import java.util.Date;
import java.util.List;

public class OptimiseResponse {

    private String signature;
    private Date createdAt;

    private String optimised;

    private List<String> missingKeys;
    private String error;
    private List<String> warnings;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
