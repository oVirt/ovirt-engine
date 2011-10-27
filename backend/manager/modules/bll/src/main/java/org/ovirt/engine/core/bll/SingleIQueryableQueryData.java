package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.utils.*;
import org.ovirt.engine.core.common.queries.*;

public class SingleIQueryableQueryData extends QueryData {
    private IVdcQueryable _cache;

    @Override
    public IRegisterQueryUpdatedData GetQueryUpdatedDataFromQueryReturnValue(VdcQueryReturnValue QueryReturnValue,
                                                                             RefObject<Boolean> changed) {
        Object tempVar = QueryReturnValue.getReturnValue();
        IVdcQueryable updatedQueryData = (IVdcQueryable) ((tempVar instanceof IVdcQueryable) ? tempVar : null);
        changed.argvalue = AreIVdcQueryablesEqual(_cache, updatedQueryData);
        _cache = updatedQueryData;
        return updatedQueryData;
    }

    public static boolean AreIVdcQueryablesEqual(IVdcQueryable first, IVdcQueryable second) {
        if (first == null && second == null) {
            return true;
        }

        if ((first == null && second != null) || (first != null && second == null)) {
            return false;
        }

        return ObjectCompareUtils.IsObjectsEqual(first, second, second.getChangeablePropertiesList());
    }
}
