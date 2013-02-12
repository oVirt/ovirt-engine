package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class ImportVmData extends ImportEntityData {

    private boolean templateExistsInSetup = true;
    private EntityModel collapseSnapshots;

    public ImportVmData(VM vm) {
        setCollapseSnapshots(new EntityModel(true));

        setEntity(vm);
        getClone().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (templateExistsInSetup) {
                    if ((Boolean) ((EntityModel) sender).getEntity()) {
                        getCollapseSnapshots().setEntity(true);
                        getCollapseSnapshots().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
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
            getCollapseSnapshots().getChangeProhibitionReasons().add(ConstantsManager.getInstance()
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
}
