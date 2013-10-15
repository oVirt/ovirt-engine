package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

public class ConsoleButtonCell extends AbstractConsoleButtonCell {

    public ConsoleButtonCell(String enabledCss, String disabledCss, String title, ConsoleButtonCommand command) {
        super(enabledCss, disabledCss, title, command);
    }

    @Override
    protected boolean shouldRenderCell(UserPortalItemModel model) {
        return model.isPool() ? false : model.getVmConsoles().canConnectToConsole();
    }
}
