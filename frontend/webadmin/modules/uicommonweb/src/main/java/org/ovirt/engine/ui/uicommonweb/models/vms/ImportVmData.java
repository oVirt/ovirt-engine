package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ImportVmData extends ImportEntityData<VM> {

    private boolean templateExistsInSetup = true;
    private EntityModel<Boolean> collapseSnapshots;
    private String warning;
    private String vmName;

    public ImportVmData(VM vm) {
        setCollapseSnapshots(new EntityModel<>(false));

        setEntity((VM) Cloner.clone(vm));
        vmName = vm.getName();
        getClone().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (templateExistsInSetup) {
                if (((EntityModel<Boolean>) sender).getEntity()) {
                    getCollapseSnapshots().setEntity(true);
                    getCollapseSnapshots().setIsChangeable(false);
                    getCollapseSnapshots().setChangeProhibitionReason(ConstantsManager.getInstance()
                            .getConstants()
                            .importCloneVMMustCollapseSnapshots());
                } else {
                    getCollapseSnapshots().setIsChangeable(true);
                }
            }
        });
    }

    public boolean isTemplateExistsInSetup() {
        return templateExistsInSetup;
    }

    public void setTemplateExistsInSetup(boolean templateExistsInSetup) {
        if (!templateExistsInSetup) {
            getCollapseSnapshots().setEntity(true);
            getCollapseSnapshots().setIsChangeable(false);
            getCollapseSnapshots().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants().importVMWithTemplateNotInSystemMustCollapseSnapshots());
        }
        this.templateExistsInSetup = templateExistsInSetup;
    }

    public VM getVm() {
        return getEntity();
    }

    public EntityModel<Boolean> getCollapseSnapshots() {
        return collapseSnapshots;
    }

    public void setCollapseSnapshots(EntityModel<Boolean> collapseSnapshots) {
        this.collapseSnapshots = collapseSnapshots;
    }

    @Override
    public ArchitectureType getArchType() {
        return getEntity().getClusterArch();
    }

    @Override
    public String getName() {
        return vmName;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
