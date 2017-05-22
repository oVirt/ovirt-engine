package org.ovirt.engine.core.bll.storage.disk.cinder;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.LibvirtSecretDao;

public class GetLibvirtSecretByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private LibvirtSecretDao libvirtSecretDao;

    public GetLibvirtSecretByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(libvirtSecretDao.get(getParameters().getId()));
    }
}
