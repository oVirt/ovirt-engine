package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForemanError implements Serializable {
    private static final long serialVersionUID = 468697212133957494L;
    private Map<String, String[]> errors;
    @JsonProperty("full_messages")
    private String[] fullMessages;

    public String[] getFullMessages() {
        return fullMessages;
    }

    public void setFullMessages(String[] fullMessages) {
        this.fullMessages = fullMessages;
    }

    public Map<String, String[]> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String[]> errors) {
        this.errors = errors;
    }
}
