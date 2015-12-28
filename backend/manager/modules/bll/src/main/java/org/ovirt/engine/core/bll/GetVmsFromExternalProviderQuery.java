package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.queries.GetVmsFromExternalProviderQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmsFromExternalProviderParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

public class GetVmsFromExternalProviderQuery<T extends GetVmsFromExternalProviderQueryParameters> extends QueriesCommandBase<T> {

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
            return runVdsCommand(VDSCommandType.GetVmsFromExternalProvider,
                    buildGetRemoteVmsInfoParameters()).getReturnValue();
        } catch (RuntimeException e) {
            if (!(e instanceof IllegalArgumentException)) {
                logFailureToGetVms();
            }
            throw e;
        }
    }

    private void logFailureToGetVms() {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("URL", getParameters().getUrl());
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_GET_EXTERNAL_VMS_INFO_FAILED);
    }

    private GetVmsFromExternalProviderParameters buildGetRemoteVmsInfoParameters() {
        return new GetVmsFromExternalProviderParameters(
                getProxyHostId(), getParameters().getUrl(),
                getParameters().getUsername(), getParameters().getPassword(),
                getParameters().getOriginType());
    }

    private Guid getProxyHostId() {
        return getParameters().getProxyHostId() != null ?
                getProxyHostIdFromParameters() : pickProxyHostFromDataCenter();
    }

    private Guid getProxyHostIdFromParameters() {
        VDS vds = getVdsDao().get(getParameters().getProxyHostId());
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
        List<VDS> vdss = getVdsDao().getAllForStoragePoolAndStatus(getParameters().getDataCenterId(), VDSStatus.Up);
        if (vdss.isEmpty()) {
            logNoProxyAvailable(getParameters().getDataCenterId());
            throw new IllegalArgumentException();
        }

        return vdss.get(0).getId();
    }

    private void logHostCannotBeProxy(String hostName) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("VdsName", hostName);
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_HOST_CANNOT_SERVE_AS_PROXY);
    }

    private void logNoProxyAvailable(Guid dataCenterId) {
        AuditLogableBase logable = new AuditLogableBase();
        logable.addCustomValue("StoragePoolName", getDbFacade().getStoragePoolDao().get(dataCenterId).getName());
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_NO_PROXY_HOST_AVAILABLE_IN_DC);
    }
}
