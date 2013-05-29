package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceHtml5;

import com.google.gwt.user.client.Window.Location;

public class SpiceHtml5Impl extends AbstractSpice implements ISpiceHtml5 {
    private final WebsocketProxyConfig config;

    public SpiceHtml5Impl() {
        super();

        this.config = new WebsocketProxyConfig(
            (String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.WebSocketProxy), getHost());
    }

    private String getClientUrl() {
        return Location.getProtocol() + "//" + Location.getHost() + //$NON-NLS-1$
                "/ovirt-engine-spicehtml5-main.html?host=" + config.getProxyHost() + //$NON-NLS-1$
                "&port=" + config.getProxyPort(); //$NON-NLS-1$
    }

    @Override
    public void connect() {
        AsyncQuery signCallback = new AsyncQuery();
        signCallback.setModel(this);
        signCallback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue queryRetVal = (VdcQueryReturnValue) returnValue;
                String signature = (String) queryRetVal.getReturnValue();

                WebClientConsoleInvoker invoker = new WebClientConsoleInvoker(signature,
                        getPassword(),
                        getClientUrl());

                invoker.invokeClientNative();
            }
        };

        boolean sslTarget = securePort == -1 ? false : true;

        Frontend.RunQuery(VdcQueryType.SignString,
                new SignStringParameters(WebClientConsoleInvoker.createConnectionString(getHost(),
                        String.valueOf(sslTarget ? securePort : port), sslTarget)),
                signCallback);
    }

    @Override
    public void install() { }

    @Override
    public boolean getIsInstalled() {
        return true;
    }
}
