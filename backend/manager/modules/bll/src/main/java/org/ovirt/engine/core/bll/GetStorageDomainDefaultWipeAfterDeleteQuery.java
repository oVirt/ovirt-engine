package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.common.queries.GetStorageDomainDefaultWipeAfterDeleteParameters;

public class GetStorageDomainDefaultWipeAfterDeleteQuery<P extends GetStorageDomainDefaultWipeAfterDeleteParameters>
        extends QueriesCommandBase<P> {

    public GetStorageDomainDefaultWipeAfterDeleteQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getStorageType() != null) {
            getQueryReturnValue().setReturnValue(
                    WipeAfterDeleteUtils.getDefaultWipeAfterDeleteFlag(getParameters().getStorageType()));
        }
    }
}
