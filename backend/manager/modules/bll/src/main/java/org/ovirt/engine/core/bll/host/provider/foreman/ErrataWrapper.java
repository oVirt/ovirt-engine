package org.ovirt.engine.core.bll.host.provider.foreman;

import org.codehaus.jackson.annotate.JsonProperty;

public class ErrataWrapper {
    @JsonProperty("results")
    private ExternalErratum[] results;

    @JsonProperty("total")
    private int totalCount;

    @JsonProperty("subtotal")
    private int subTotalCount;

    public ExternalErratum[] getResults() {
        return results;
    }

    public void setResults(ExternalErratum[] errata) {
        this.results = errata;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSubTotalCount() {
        return subTotalCount;
    }

    public void setSubTotalCount(int subTotalCount) {
        this.subTotalCount = subTotalCount;
    }
}
