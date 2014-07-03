package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.annotate.JsonProperty;

public class ForemanComputerResourceWrapper {
    @JsonProperty("results")
    private ForemanComputerResource[] results;

    public ForemanComputerResource[] getResults() {
        return results;
    }

    public void setResults(ForemanComputerResource[] host) {
        this.results = host;
    }
}
