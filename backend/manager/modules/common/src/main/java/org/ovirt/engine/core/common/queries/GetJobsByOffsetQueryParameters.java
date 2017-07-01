package org.ovirt.engine.core.common.queries;


public class GetJobsByOffsetQueryParameters  extends QueryParametersBase {

    private static final long serialVersionUID = -536423247558047939L;

    private int offset;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

}
