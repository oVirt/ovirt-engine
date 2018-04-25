package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmBasedWidgetSwitchModeCommand extends UICommand {
    // this class is here just because constructor can not call super by referencing itself, so there must be a middle man
    private static class AdvancedButtonTarget implements ICommandTarget {

        @Override
        public void executeCommand(UICommand command) {
            VmBasedWidgetSwitchModeCommand button = (VmBasedWidgetSwitchModeCommand) command;
            button.advancedClicked();
        }

        @Override
        public void executeCommand(UICommand uiCommand, Object... parameters) {
            // nothing to do
        }
    }

    public static final String NAME = "OnAdvanced"; //$NON-NLS-1$

    private UnitVmModel model;

    public VmBasedWidgetSwitchModeCommand() {
        super(NAME, new AdvancedButtonTarget());
        setIsDefault(false);
    }

    public void init(UnitVmModel model) {
        this.model = model;
        setTitle(determineAdvancedOptionsButtonLabel());
    }

    void advancedClicked() {
        boolean advancedModeEnabled = isAdvancedMode();
        setAdvancedMode(!advancedModeEnabled);
        // a rename has to be done this way otherwise ignored
        setTitle(null);
        setTitle(determineAdvancedOptionsButtonLabel());
    }

    private String determineAdvancedOptionsButtonLabel() {
        if (isAdvancedMode()) {
            return ConstantsManager.getInstance().getConstants().hideAdvancedOptions();
        } else {
            return ConstantsManager.getInstance().getConstants().showAdvancedOptions();
        }
    }

    private boolean isAdvancedMode() {
        return model.getAdvancedMode().getEntity();
    }

    private void setAdvancedMode(boolean advancedMode) {
        model.getAdvancedMode().setEntity(advancedMode);
    }
}
