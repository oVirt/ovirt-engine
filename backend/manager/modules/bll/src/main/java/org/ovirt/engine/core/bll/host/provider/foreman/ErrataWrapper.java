package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.annotate.JsonProperty;

public class ErrataWrapper {
    @JsonProperty("results")
    private ExternalErratum[] results;

    public ExternalErratum[] getResults() {
        return results;
    }

    public void setResults(ExternalErratum[] errata) {
        this.results = errata;
    }
}
