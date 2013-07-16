package org.ovirt.engine.ui.userportal.widget.extended.vm;

import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;

public class ConsoleButtonCell extends AbstractConsoleButtonCell {

    private final ConsoleUtils consoleUtils;

    public ConsoleButtonCell(ConsoleUtils consoleUtils, String enabledCss, String disabledCss, String title, ConsoleButtonCommand command) {
        super(enabledCss, disabledCss, title, command);

        this.consoleUtils = consoleUtils;
    }

    @Override
    protected boolean shouldRenderCell(UserPortalItemModel model) {
        ConsoleProtocol protocol = consoleUtils.determineConnectionProtocol(model);
        return consoleUtils.canShowConsole(protocol, model);
    }
}
