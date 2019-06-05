package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.queries.GetVmChangedFieldsForNextRunParameters;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;

public class GetVmChangedFieldsForNextRunQuery<P extends GetVmChangedFieldsForNextRunParameters>
        extends QueriesCommandBase<P>{

    @Inject
    private VmHandler vmHandler;

    public GetVmChangedFieldsForNextRunQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM srcVm = getParameters().getOriginal();
        VM dstVm = getParameters().getUpdated();
        VmStatic srcStatic = srcVm.getStaticData();
        VmStatic dstStatic = dstVm.getStaticData();

        // Copy creationDate to ignore it, because it is never changed by user.
        // Without this creationDate will always show change in milliseconds,
        // because creationDate is saved without milliseconds in OVF, but
        // with milliseconds in the DB.
        dstStatic.setCreationDate(srcStatic.getCreationDate());

        // Hot plug CPU, memory and VmLease are displayed separately in the confirmation dialog,
        // so it is not needed to include them into changed fields list.
        if (VmCommonUtils.isCpusToBeHotpluggedOrUnplugged(srcVm, dstVm)) {
            dstStatic.setNumOfSockets(srcStatic.getNumOfSockets());
        }
        if (VmCommonUtils.isMemoryToBeHotplugged(srcVm, dstVm)) {
            dstStatic.setMemSizeMb(srcStatic.getMemSizeMb());
            dstStatic.setMinAllocatedMem(srcStatic.getMinAllocatedMem());
        }
        if (VmCommonUtils.isVmLeaseToBeHotPluggedOrUnplugged(srcVm, dstVm)) {
            dstStatic.setLeaseStorageDomainId(srcStatic.getLeaseStorageDomainId());
        }

        VmPropertiesUtils vmPropertiesUtils = SimpleDependencyInjector.getInstance().get(VmPropertiesUtils.class);

        vmPropertiesUtils.separateCustomPropertiesToUserAndPredefined(
                srcVm.getCompatibilityVersion(), srcStatic);
        vmPropertiesUtils.separateCustomPropertiesToUserAndPredefined(
                dstVm.getCompatibilityVersion(), dstStatic);

        setReturnValue(new ArrayList<>(vmHandler.getChangedFieldsForStatus(srcStatic, dstStatic, getParameters().getUpdateVmParameters(), VMStatus.Up)));
    }
}
