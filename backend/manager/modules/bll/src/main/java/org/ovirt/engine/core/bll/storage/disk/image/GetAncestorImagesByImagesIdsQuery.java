package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAncestorImagesByImagesIdsQuery<P extends IdsQueryParameters>
        extends QueriesCommandBase<P> {

    public GetAncestorImagesByImagesIdsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Map<Guid, DiskImage> imagesAncestors = new HashMap<>();
        for (Guid id : getParameters().getIds()) {
            DiskImage ancestor = getDbFacade().getDiskImageDao().getAncestor(
                    id, getUserID(), getParameters().isFiltered());
            imagesAncestors.put(id, ancestor);
        }
        getQueryReturnValue().setReturnValue(imagesAncestors);
    }
}
