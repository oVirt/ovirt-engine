package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.WipeAfterDeleteUtils;
import org.ovirt.engine.core.common.queries.GetStorageDomainDefaultWipeAfterDeleteParameters;

public class GetStorageDomainDefaultWipeAfterDeleteQuery<P extends GetStorageDomainDefaultWipeAfterDeleteParameters>
        extends QueriesCommandBase<P> {

    public GetStorageDomainDefaultWipeAfterDeleteQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getStorageType() != null) {
            getQueryReturnValue().setReturnValue(
                    WipeAfterDeleteUtils.getDefaultWipeAfterDeleteFlag(getParameters().getStorageType()));
        }
    }
}
