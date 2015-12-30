package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetErrataCountsParameters extends IdQueryParameters{

    private ErrataFilter errataFilter;

    public GetErrataCountsParameters(){
    }

    public GetErrataCountsParameters(Guid id){
        super(id);
    }

    public ErrataFilter getErrataFilter() {
        return errataFilter;
    }

    public void setErrataFilter(ErrataFilter errataFilter) {
        this.errataFilter = errataFilter;
    }
}
