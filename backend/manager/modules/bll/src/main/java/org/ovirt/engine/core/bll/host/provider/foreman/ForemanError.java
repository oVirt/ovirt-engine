package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

public class ForemanError implements Serializable {
    private static final long serialVersionUID = 468697212133957494L;
    private Map<String, String[]> errors;
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
