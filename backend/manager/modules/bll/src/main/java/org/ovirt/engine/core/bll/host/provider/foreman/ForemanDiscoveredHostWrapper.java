package org.ovirt.engine.core.bll.host.provider.foreman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForemanDiscoveredHostWrapper {
    @JsonProperty("results")
    private ForemanDiscoveredHost[] results;

    public ForemanDiscoveredHost[] getResults() {
        return results;
    }

    public void setResults(ForemanDiscoveredHost[] host) {
        this.results = host;
    }
}
