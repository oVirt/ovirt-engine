package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;

public class NoVncImpl extends AbstractVnc implements INoVnc {

    private static final String CLIENT_PAGE = BaseContextPathData.getInstance().getRelativePath()
            + "services/novnc-main.html"; //$NON-NLS-1$
    private final WebsocketProxyConfig config;

    public NoVncImpl() {
        this.config = new WebsocketProxyConfig(
                (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.WebSocketProxy), getVncHost());
    }

    @Override
    public void invokeClient() {
        WebClientConsoleInvoker invoker = new WebClientConsoleInvoker(CLIENT_PAGE, config, getVncHost(), getVncPort(), getTicket(), false);
        invoker.invokeClient();
    }

}
