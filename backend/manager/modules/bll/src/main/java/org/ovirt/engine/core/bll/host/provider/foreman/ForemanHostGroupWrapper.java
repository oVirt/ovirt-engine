package org.ovirt.engine.core.bll.host.provider.foreman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForemanHostGroupWrapper {
    @JsonProperty("results")
    private ForemanHostGroup[] results;

    public ForemanHostGroup[] getResults() {
        return results;
    }

    public void setResults(ForemanHostGroup[] hostGroup) {
        this.results = hostGroup;
    }
}
