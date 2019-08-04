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
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

@NonTransactiveCommandAttribute
public class RemoveUnregisteredVmTemplateCommand<T extends RemoveUnregisteredEntityParameters> extends
        RemoveUnregisteredEntityCommand<T> {

    @Inject
    private OvfHelper ovfHelper;

    private VmTemplate vmTemplateFromConfiguration;

    public RemoveUnregisteredVmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setUnregisteredEntityAndImages() throws OvfReaderException {
        FullEntityOvfData fullEntityOvfData = ovfHelper.readVmTemplateFromOvf(ovfEntityData.getOvfData());
        vmTemplateFromConfiguration = fullEntityOvfData.getVmTemplate();
        setVmTemplate(vmTemplateFromConfiguration);
        images = fullEntityOvfData.getDiskImages();
    }

    @Override
    protected boolean isUnregisteredEntityExists(){
        return vmTemplateFromConfiguration != null;
    }

    @Override
    protected EngineMessage getEntityNotExistsMessage() {
        return EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST;
    }

    @Override
    protected EngineMessage getRemoveAction() {
        return EngineMessage.VAR__TYPE__VM_TEMPLATE;
    }

    @Override
    protected EntityInfo getEntityInfo() {
        return new EntityInfo(VdcObjectType.VmTemplate, getVmTemplateId());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE
                : AuditLogType.IMPORTEXPORT_REMOVE_TEMPLATE_FAILED;
    }

    @Override
    public VmTemplate getVmTemplate() {
        return vmTemplateFromConfiguration;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            jobProperties.put("vmtemplatename", (getVmTemplateName() == null) ? "" : getVmTemplateName());
            jobProperties.put(VdcObjectType.Storage.name().toLowerCase(), getStorageDomainName());
        }
        return jobProperties;
    }
}
