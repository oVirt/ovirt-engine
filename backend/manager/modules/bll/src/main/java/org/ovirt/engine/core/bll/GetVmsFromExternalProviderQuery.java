package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmsFromExternalProviderParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.di.Injector;

public class GetVmsFromExternalProviderQuery<T extends GetVmsFromExternalProviderQueryParameters>
        extends QueriesCommandBase<T> {

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private VdsDao vdsDao;

    public GetVmsFromExternalProviderQuery(T parameters) {
        this(parameters, null);
    }

    public GetVmsFromExternalProviderQuery(T parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(getVmsFromExternalProvider());
    }

    private Object getVmsFromExternalProvider() {
        try {
            if (isGetNamesOfVmsFromExternalProviderSupported() && getParameters().getNamesOfVms() == null) {
                return runVdsCommand(VDSCommandType.GetVmsNamesFromExternalProvider,
                        buildGetRemoteVmsInfoParameters()).getReturnValue();
            } else {
                return runVdsCommand(VDSCommandType.GetVmsFullInfoFromExternalProvider,
                        buildGetRemoteVmsInfoParameters()).getReturnValue();
            }

        } catch (RuntimeException e) {
            if (!(e instanceof IllegalArgumentException)) {
                logFailureToGetVms();
            }
            throw e;
        }
    }

    private void logFailureToGetVms() {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
        logable.addCustomValue("URL", getParameters().getUrl());
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_GET_EXTERNAL_VMS_INFO_FAILED);
    }

    private GetVmsFromExternalProviderParameters buildGetRemoteVmsInfoParameters() {
        return new GetVmsFromExternalProviderParameters(
                getProxyHostId(),
                getParameters().getUrl(),
                getParameters().getUsername(),
                getParameters().getPassword(),
                getParameters().getOriginType(),
                getParameters().getNamesOfVms());
    }

    private Guid getProxyHostId() {
        return getParameters().getProxyHostId() != null ?
                getProxyHostIdFromParameters() : pickProxyHostFromDataCenter();
    }

    private Guid getProxyHostIdFromParameters() {
        VDS vds = vdsDao.get(getParameters().getProxyHostId());
        if (vds == null) {
            throw new IllegalArgumentException(
                    String.format("No VDS with the given ID '%s' exists", getParameters().getProxyHostId()));
        }

        if (vds.getStatus() != VDSStatus.Up) {
            logHostCannotBeProxy(vds.getName());
            throw new IllegalArgumentException();
        }

        return vds.getId();
    }

    private Guid pickProxyHostFromDataCenter() {
        Guid dataCenterId = getParameters().getDataCenterId();
        List<VDS> vdss = vdsDao.getAllForStoragePoolAndStatus(dataCenterId, VDSStatus.Up);
        if (vdss.isEmpty()) {
            logNoProxyAvailable(dataCenterId);
            throw new IllegalArgumentException();
        }

        return vdss.get(0).getId();
    }

    private void logHostCannotBeProxy(String hostName) {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
        logable.addCustomValue("VdsName", hostName);
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_HOST_CANNOT_SERVE_AS_PROXY);
    }

    private void logNoProxyAvailable(Guid dataCenterId) {
        AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
        String dcName = storagePoolDao.get(dataCenterId).getName();
        logable.addCustomValue("StoragePoolName", dcName);
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_NO_PROXY_HOST_AVAILABLE_IN_DC);
    }

    private boolean isGetNamesOfVmsFromExternalProviderSupported() {
        return FeatureSupported.isGetNamesOfVmsFromExternalProviderSupported(storagePoolDao
                .get(getParameters().getDataCenterId())
                .getCompatibilityVersion());
    }
}
