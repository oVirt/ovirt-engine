package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomainsQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DiskImageDao diskImageDao;

    public GetAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomainsQuery(
            P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Guid> metadataAndMemoryVolumesToTransfer =
                diskImageDao.getAllMetadataAndMemoryDisksOfSnapshotsOnDifferentStorageDomains(getParameters().getId());
        if (metadataAndMemoryVolumesToTransfer == null) {
            setReturnValue(Collections.emptyList());
        } else {
            setReturnValue(metadataAndMemoryVolumesToTransfer);
        }
    }
}
