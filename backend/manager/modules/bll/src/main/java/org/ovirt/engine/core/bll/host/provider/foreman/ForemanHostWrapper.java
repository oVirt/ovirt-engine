package org.ovirt.engine.core.bll.host.provider.foreman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForemanHostWrapper {
    @JsonProperty("results")
    private ForemanHost[] results;

    public ForemanHost[] getResults() {
        return results;
    }

    public void setResults(ForemanHost[] hosts) {
        this.results = hosts;
    }
}
