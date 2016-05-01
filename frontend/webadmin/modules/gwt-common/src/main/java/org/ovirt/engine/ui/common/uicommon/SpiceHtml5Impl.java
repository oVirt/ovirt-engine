package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceHtml5;

public class SpiceHtml5Impl extends AbstractSpice implements ISpiceHtml5 {

    private static final String CLIENT_PAGE = BaseContextPathData.getRelativePath()
            + "services/spicehtml5-main.html"; //$NON-NLS-1$

    private static final String SPICEHTML5_TITLE_SUFFIX = " - Spice Javascript Client"; //$NON-NLS-1$

    private WebsocketProxyConfig config;

    @Override
    public void invokeClient() {
        boolean sslTarget = consoleOptions.getRawSecurePort() != -1;
        int port = sslTarget ? consoleOptions.getSecurePort() : consoleOptions.getPort();
        WebClientConsoleInvoker invoker = new WebClientConsoleInvoker(CLIENT_PAGE, getConfig(), getOptions().getHost(), port, getOptions().getTicket(), sslTarget, getOptions().getVmName() + SPICEHTML5_TITLE_SUFFIX);
        invoker.invokeClient();
    }

    protected WebsocketProxyConfig getConfig() {
        if (config == null) {
            config = new WebsocketProxyConfig(
                    (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.WebSocketProxy),
                    getOptions().getHost());
        }
        return config;
    }
}
