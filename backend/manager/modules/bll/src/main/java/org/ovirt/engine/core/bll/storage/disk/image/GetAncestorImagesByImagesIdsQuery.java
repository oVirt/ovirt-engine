package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

public class GetAncestorImagesByImagesIdsQuery<P extends IdsQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private DiskImageDao diskImageDao;

    public GetAncestorImagesByImagesIdsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<Guid, DiskImage> imagesAncestors = new HashMap<>();
        for (Guid id : getParameters().getIds()) {
            DiskImage ancestor = diskImageDao.getAncestor(id, getUserID(), getParameters().isFiltered());
            imagesAncestors.put(id, ancestor);
        }
        getQueryReturnValue().setReturnValue(imagesAncestors);
    }
}
