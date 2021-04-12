package org.ovirt.engine.ui.uicommonweb.builders.template;

import org.ovirt.engine.core.common.action.AddVmTemplateParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class UnitToAddVmTemplateParametersBuilder<T extends AddVmTemplateParameters> extends BaseSyncBuilder<UnitVmModel, T> {

    @Override
    protected void build(UnitVmModel source, T destination) {
        destination.setPublicUse(source.getIsTemplatePublic().getEntity());
        destination.setDiskInfoDestinationMap(
                source.getDisksAllocationModel().getImageToDestinationDomainMap());
        destination.setSoundDeviceEnabled(source.getIsSoundcardEnabled().getEntity());
        destination.setTpmEnabled(source.getTpmEnabled().getEntity());
        destination.setCopyVmPermissions(source.getCopyPermissions().getEntity());
        destination.setSealTemplate(source.getSealTemplate().getEntity());
        destination.setConsoleEnabled(source.getIsConsoleDeviceEnabled().getEntity());
        if (source.getIsSubTemplate().getEntity()) {
            destination.setBaseTemplateId(source.getBaseTemplate().getSelectedItem().getId());
            destination.setTemplateVersionName(source.getTemplateVersionName().getEntity());
        }

        destination.setVirtioScsiEnabled(source.getIsVirtioScsiEnabled().getEntity());
        if(source.getIsHeadlessModeEnabled().getEntity()) {
            destination.getMasterVm().setDefaultDisplayType(DisplayType.none);
        }
    }

}
