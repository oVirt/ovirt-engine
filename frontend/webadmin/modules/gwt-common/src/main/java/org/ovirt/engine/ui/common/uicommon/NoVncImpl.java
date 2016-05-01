package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;

public class NoVncImpl extends AbstractVnc implements INoVnc {

    private static final String CLIENT_PAGE = BaseContextPathData.getRelativePath()
            + "services/novnc-main.html"; //$NON-NLS-1$

    private static final String NOVNC_TITLE_SUFFIX = " - noVNC"; //$NON-NLS-1$

    private WebsocketProxyConfig config;

    @Override
    public void invokeClient() {
            WebClientConsoleInvoker invoker =
                    new WebClientConsoleInvoker(CLIENT_PAGE, getConfig(),
                            getOptions().getHost(), getOptions().getPort(),
                            getOptions().getTicket(), false, getOptions().getVmName() + NOVNC_TITLE_SUFFIX);
        invoker.invokeClient();
    }

    protected WebsocketProxyConfig getConfig() {
        if (config == null) {
            config = new WebsocketProxyConfig((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.WebSocketProxy),
                    getOptions().getHost());
        }
        return config;
    }
}
