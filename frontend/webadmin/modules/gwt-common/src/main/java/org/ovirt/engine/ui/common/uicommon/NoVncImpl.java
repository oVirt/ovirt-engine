package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;

public class NoVncImpl extends AbstractVnc implements INoVnc {

    private static final String CLIENT_PAGE = BaseContextPathData.getRelativePath()
            + "services/novnc-main.jsp"; //$NON-NLS-1$

    private static final String NOVNC_TITLE_SUFFIX = " - noVNC"; //$NON-NLS-1$

    private WebsocketProxyConfig config;

    @Override
    public void invokeClient() {
            WebClientConsoleInvoker invoker =
                    new WebClientConsoleInvoker(CLIENT_PAGE,
                            getConfig(),
                            getOptions().getHost(),
                            getOptions().getPort(),
                            getOptions().getTicket(),
                            getOptions().isUseSsl(),
                            getOptions().getVmName() + NOVNC_TITLE_SUFFIX,
                            getOptions().getPath(),
                            getOptions().getToken());
        invoker.invokeClient();
    }

    protected WebsocketProxyConfig getConfig() {
        if (config == null) {
            config = new WebsocketProxyConfig((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.WebSocketProxy),
                    getOptions().getHost());
        }
        return config;
    }
}
