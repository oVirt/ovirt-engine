package org.ovirt.engine.core.bll.exportimport;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveUnregisteredEntityParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

@NonTransactiveCommandAttribute
public class RemoveUnregisteredVmCommand<T extends RemoveUnregisteredEntityParameters> extends
        RemoveUnregisteredEntityCommand<T> {

    @Inject
    private OvfHelper ovfHelper;

    private VM vmFromConfiguration;

    public RemoveUnregisteredVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setUnregisteredEntityAndImages() throws OvfReaderException {
        FullEntityOvfData fullEntityOvfData = ovfHelper.readVmFromOvf(ovfEntityData.getOvfData());
        vmFromConfiguration = fullEntityOvfData.getVm();
        setVm(vmFromConfiguration);
        images = fullEntityOvfData.getDiskImages();
    }

    @Override
    protected boolean isUnregisteredEntityExists(){
        return vmFromConfiguration != null;
    }

    @Override
    protected EngineMessage getEntityNotExistsMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_VM_NOT_EXIST;
    }

    @Override
    protected EngineMessage getRemoveAction() {
        return EngineMessage.VAR__TYPE__VM;
    }

    @Override
    protected EntityInfo getEntityInfo() {
        return new EntityInfo(VdcObjectType.VM, getVmId());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.IMPORTEXPORT_REMOVE_VM
                : AuditLogType.IMPORTEXPORT_REMOVE_VM_FAILED;
    }

    @Override
    public VM getVm() {
        return vmFromConfiguration;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            jobProperties.put("vmname", (getVmName() == null) ? "" : getVmName());
            jobProperties.put(VdcObjectType.Storage.name().toLowerCase(), getStorageDomainName());
        }
        return jobProperties;
    }
}
