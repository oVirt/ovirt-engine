package org.ovirt.engine.core.bll.host.provider.foreman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForemanErrorWrapper {
    @JsonProperty("error")
    private ForemanError fe;

    public ForemanError getForemanError() {
        return fe;
    }

    public void setForemanError(ForemanError fe) {
        this.fe = fe;
    }
}
