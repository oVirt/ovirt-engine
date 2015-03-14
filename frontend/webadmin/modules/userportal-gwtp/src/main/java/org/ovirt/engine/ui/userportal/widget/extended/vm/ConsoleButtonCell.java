package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

public class ConsoleButtonCell extends AbstractConsoleButtonCell {

    public ConsoleButtonCell(String enabledCss, String disabledCss, ConsoleButtonCommand command) {
        super(enabledCss, disabledCss, command);
    }

    @Override
    protected boolean shouldRenderCell(UserPortalItemModel model) {
        return model.isPool() ? false : model.getVmConsoles().canConnectToConsole();
    }
}
