package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdsQueryParameters;
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
        getQueryReturnValue().setReturnValue(getParameters().getIds().stream()
                .collect(Collectors.toMap(Function.identity(),
                        id -> diskImageDao.getAncestor(id, getUserID(), getParameters().isFiltered()))));
    }
}
