package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.GetVmsFromExternalProviderParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.Injector;

public class GetVmsFullInfoFromExternalProviderVDSCommand<T extends GetVmsFromExternalProviderParameters>
        extends VdsBrokerCommand<T> {
    private VMListReturn vmListReturn;

    @Inject
    private AuditLogDirector auditLogDirector;

    public GetVmsFullInfoFromExternalProviderVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getExternalVmList(getParameters().getUrl(),
                getParameters().getUsername(), getParameters().getPassword(), getParameters().getNamesOfVms());
        proceedProxyReturnValue();
        List<VM> vms = new ArrayList<>();
        List<VM> notDownVms = new ArrayList<>();
        for (Map<String, Object> map : vmListReturn.vmList) {
            VM vm = VdsBrokerObjectsBuilder.buildVmsDataFromExternalProvider(map);
            if (vm != null) {
                vm.setOrigin(getParameters().getOriginType());
                vms.add(vm);
                // identify vms not in Down status
                if (!vm.isDown()) {
                    notDownVms.add(vm);
                }
            }
        }

        logNonDownVms(notDownVms);
        setReturnValue(vms);
    }

    @Override
    protected Status getReturnStatus() {
        return vmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }

    private void logNonDownVms(List<VM> notDownVms) {
        if (!notDownVms.isEmpty()) {
            if (shouldLogToAuditLog()) {
                AuditLogableBase logable = Injector.injectMembers(new AuditLogableBase());
                logable.addCustomValue("URL", getParameters().getUrl());
                logable.addCustomValue("Vms", StringUtils.join(notDownVms, ","));
                auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_GET_EXTERNAL_VMS_NOT_IN_DOWN_STATUS);
            } else {
                log.warn(
                        "The following VMs retrieved from external server '{}' are not in down status and therefore can't be imported: '{}'.",
                        getParameters().getUrl(),
                        StringUtils.join(notDownVms, ","));
            }
        }
    }

    private boolean shouldLogToAuditLog() {
        return CollectionUtils.isNotEmpty(getParameters().getNamesOfVms());
    }
}
