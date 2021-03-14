package org.ovirt.engine.core.bll.host.provider.foreman;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentHostsV30Wrapper {
    @JsonProperty("results")
    private ContentHostV30[] results;

    public ContentHostV30[] getResults() {
        return results;
    }

    public void setResults(ContentHostV30[] hosts) {
        this.results = hosts;
    }
}
