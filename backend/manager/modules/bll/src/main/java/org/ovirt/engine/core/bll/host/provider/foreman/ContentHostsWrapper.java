package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.annotate.JsonProperty;

public class ContentHostsWrapper {
    @JsonProperty("results")
    private ContentHost[] results;

    public ContentHost[] getResults() {
        return results;
    }

    public void setResults(ContentHost[] hosts) {
        this.results = hosts;
    }
}
