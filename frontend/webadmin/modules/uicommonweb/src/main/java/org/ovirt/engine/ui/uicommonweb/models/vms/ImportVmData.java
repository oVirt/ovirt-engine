package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class ImportVmData extends ImportEntityData<VM> {

    private boolean templateExistsInSetup = true;
    private EntityModel<Boolean> collapseSnapshots;
    private String warning;
    private String error;
    private boolean nameExistsInTheSystem;
    private String vmName;

    public ImportVmData(VM vm) {
        setCollapseSnapshots(new EntityModel<>(true));

        setEntity(vm);
        vmName = vm.getName();
        getClone().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (templateExistsInSetup) {
                    if (((EntityModel<Boolean>) sender).getEntity()) {
                        getCollapseSnapshots().setEntity(true);
                        getCollapseSnapshots().setChangeProhibitionReason(ConstantsManager.getInstance()
                                .getConstants()
                                .importCloneVMMustCollapseSnapshots());
                        getCollapseSnapshots().setIsChangeable(false);
                    }
                    else {
                        getCollapseSnapshots().setIsChangeable(true);
                    }
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
            getCollapseSnapshots().setChangeProhibitionReason(ConstantsManager.getInstance()
                    .getConstants().importVMWithTemplateNotInSystemMustCollapseSnapshots());
            getCollapseSnapshots().setIsChangeable(false);
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isNameExistsInTheSystem() {
        return nameExistsInTheSystem;
    }

    public void setNameExistsInTheSystem(boolean nameExistsInTheSystem) {
        this.nameExistsInTheSystem = nameExistsInTheSystem;
    }
}
