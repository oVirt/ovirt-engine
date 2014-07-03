package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.annotate.JsonProperty;

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
