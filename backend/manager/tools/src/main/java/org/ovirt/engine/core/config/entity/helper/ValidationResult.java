package org.ovirt.engine.core.config.entity.helper;

/**
 * Represents the result of the Value Helper validation
 */
public class ValidationResult {


    public ValidationResult(boolean ok) {
        this(ok, "");
    }

    public ValidationResult(boolean ok, String details) {
        this.ok = ok;
        this.details = details;
    }

    private boolean ok;
    private String details;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}
