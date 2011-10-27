package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.SearchReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public class SearchData extends ListIQueryableQueryData {
    @Override
    public boolean IsQueryValid(VdcQueryReturnValue queryRetValue) {
        return ((SearchReturnValue) queryRetValue).getIsSearchValid();
    }
}
