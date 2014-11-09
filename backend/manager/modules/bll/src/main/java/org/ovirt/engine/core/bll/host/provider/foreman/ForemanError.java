package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;
import java.util.Map;

public class ForemanError implements Serializable {
    private static final long serialVersionUID = 468697212133957494L;
    private Map<String, String[]> errors;
    private String[] full_messages;

    public String[] getFull_messages() { return full_messages; }

    public void setFull_messages(String[] full_messages) { this.full_messages = full_messages; }

    public Map<String, String[]> getErrors() { return errors; }

    public void setErrors(Map<String, String[]> errors) { this.errors = errors; }
}
