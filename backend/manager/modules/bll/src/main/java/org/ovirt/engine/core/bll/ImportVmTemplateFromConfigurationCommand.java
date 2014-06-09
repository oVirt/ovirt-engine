package org.ovirt.engine.core.bll;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ImportVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

@LockIdNameAttribute
@NonTransactiveCommandAttribute
public class ImportVmTemplateFromConfigurationCommand<T extends ImportVmTemplateParameters> extends ImportVmTemplateCommand {

    private static final Log log = LogFactory.getLog(ImportVmFromConfigurationCommand.class);
    private OvfEntityData ovfEntityData;
    VmTemplate vmTemplateFromConfiguration;

    protected ImportVmTemplateFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmTemplateFromConfigurationCommand(T parameters) {
        super(parameters);
        setCommandShouldBeLogged(false);
    }

    @Override
    public Guid getVmTemplateId() {
        if (isImagesAlreadyOnTarget()) {
            return getParameters().getContainerId();
        }
        return super.getVmTemplateId();
    }

    @Override
    protected boolean canDoAction() {
        initVmTemplate();
        if (isImagesAlreadyOnTarget() && !validateUnregisteredEntity(vmTemplateFromConfiguration, ovfEntityData)) {
            return false;
        }
        ArrayList<DiskImage> disks = new ArrayList(getVmTemplate().getDiskTemplateMap().values());
        setImagesWithStoragePoolId(getStorageDomain().getStoragePoolId(), disks);
        getParameters().setImages(disks);
        getVmTemplate().setImages(disks);
        return super.canDoAction();
    }

    private void initVmTemplate() {
        OvfHelper ovfHelper = new OvfHelper();
        ovfEntityData =
                getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(getParameters().getContainerId(),
                        getParameters().getStorageDomainId());
        if (ovfEntityData != null) {
            try {
                vmTemplateFromConfiguration = ovfHelper.readVmTemplateFromOvf(ovfEntityData.getOvfData());
                vmTemplateFromConfiguration.setVdsGroupId(getParameters().getVdsGroupId());
                setVmTemplate(vmTemplateFromConfiguration);
                getParameters().setVmTemplate(vmTemplateFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());
            } catch (OvfReaderException e) {
                log.errorFormat("failed to parse a given ovf configuration: \n" + ovfEntityData.getOvfData(), e);
            }
        }
        setVdsGroupId(getParameters().getVdsGroupId());
        setStoragePoolId(getVdsGroup().getStoragePoolId());
    }

    @Override
    public void executeCommand() {
        super.executeCommand();
        if (isImagesAlreadyOnTarget()) {
            getUnregisteredOVFDataDao().removeEntity(ovfEntityData.getEntityId(), ovfEntityData.getStorageDomainId());
        }
        setActionReturnValue(getVmTemplate().getId());
    }
}
