package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmsInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StorageDomainDao;

public abstract class GetAllFromExportDomainQuery <T, P extends GetAllFromExportDomainQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public GetAllFromExportDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    protected abstract T buildFromOVFs(List<String> ovfList);

    @Override
    protected final void executeQueryCommand() {
        StorageDomain storage = getStorage();
        T returnValue = getAllFromStorage(storage);
        getQueryReturnValue().setReturnValue(returnValue);
    }

    private StorageDomain getStorage() {
        return storageDomainDao.getForStoragePool(
                getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
    }

    private T getAllFromStorage(StorageDomain storage) {
        return buildFromOVFs(isValidExportDomain(storage) ?
                (List<String>) executeVerb(storage.getStorageStaticData()).getReturnValue()
                : Collections.emptyList());
    }

    private boolean isValidExportDomain(StorageDomain storage) {
        return storage != null
                && storage.getStorageDomainType() == StorageDomainType.ImportExport;
    }

    private VDSReturnValue executeVerb(StorageDomainStatic storage) {
        try {
            return runVdsCommand(VDSCommandType.GetVmsInfo, buildGetVmsInfoParameters(storage));
        } catch (RuntimeException e) {
            AuditLogable logable = new AuditLogableImpl();
            logable.setStorageDomainId(storage.getId());
            logable.setStorageDomainName(storage.getName());
            auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_GET_VMS_INFO_FAILED);
            throw e;
        }
    }

    private GetVmsInfoVDSCommandParameters buildGetVmsInfoParameters(StorageDomainStatic storage) {
        GetVmsInfoVDSCommandParameters parameters =
                new GetVmsInfoVDSCommandParameters(getParameters().getStoragePoolId());
        parameters.setStorageDomainId(storage.getId());
        parameters.setVmIdList(getParameters().getIds());
        return parameters;
    }
}
