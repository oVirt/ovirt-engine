package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceHtml5;

public class SpiceHtml5Impl extends AbstractSpice implements ISpiceHtml5 {

    private static final String CLIENT_PAGE = BaseContextPathData.getInstance().getRelativePath()
            + "services/spicehtml5-main.html"; //$NON-NLS-1$
    private final WebsocketProxyConfig config;

    public SpiceHtml5Impl() {
        this.config = new WebsocketProxyConfig(
                (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.WebSocketProxy),
                getOptions().getHost());
    }

    @Override
    public void invokeClient() {
        boolean sslTarget = consoleOptions.getSecurePort() == -1 ? false : true;
        int port = sslTarget ? consoleOptions.getSecurePort() : consoleOptions.getPort();
        WebClientConsoleInvoker invoker =
                new WebClientConsoleInvoker(CLIENT_PAGE, config, getOptions().getHost(), port, getOptions().getTicket(), sslTarget);
        invoker.invokeClient();
    }

}
