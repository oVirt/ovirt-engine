package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

public class ImportVmData extends ImportEntityData {

    private boolean templateExistsInSetup = true;
    private EntityModel collapseSnapshots;

    public ImportVmData(VM vm) {
        setCollapseSnapshots(new EntityModel(true));

        setEntity(vm);
        getClone().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (templateExistsInSetup) {
                    if (((EntityModel<Boolean>) sender).getEntity()) {
                        getCollapseSnapshots().setEntity(true);
                        getCollapseSnapshots().setChangeProhibitionReason(ConstantsManager.getInstance()
                                .getConstants()
                                .importCloneVMMustCollapseSnapshots());
                        getCollapseSnapshots().setIsChangable(false);
                    }
                    else {
                        getCollapseSnapshots().setIsChangable(true);
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
            getCollapseSnapshots().setIsChangable(false);
        }
        this.templateExistsInSetup = templateExistsInSetup;
    }

    public VM getVm() {
        return (VM) getEntity();
    }

    public EntityModel getCollapseSnapshots() {
        return collapseSnapshots;
    }

    public void setCollapseSnapshots(EntityModel collapseSnapshots) {
        this.collapseSnapshots = collapseSnapshots;
    }

    @Override
    public ArchitectureType getArchType() {
        return ((VM) getEntity()).getClusterArch();
    }

    @Override
    public String getName() {
        return ((VM) getEntity()).getName();
    }
}
