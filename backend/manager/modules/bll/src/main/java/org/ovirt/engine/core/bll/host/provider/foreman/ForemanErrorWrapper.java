package org.ovirt.engine.core.bll.host.provider.foreman;
import org.codehaus.jackson.annotate.JsonProperty;

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
