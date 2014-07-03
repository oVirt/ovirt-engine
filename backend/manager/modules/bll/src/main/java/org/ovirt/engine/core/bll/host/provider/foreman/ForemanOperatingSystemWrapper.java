package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.annotate.JsonProperty;

public class ForemanOperatingSystemWrapper {
    @JsonProperty("results")
    private ForemanOperatingSystem[] results;

    public ForemanOperatingSystem[] getResults() {
        return results;
    }

    public void setResults(ForemanOperatingSystem[] operationsystem) {
        this.results = operationsystem;
    }
}
