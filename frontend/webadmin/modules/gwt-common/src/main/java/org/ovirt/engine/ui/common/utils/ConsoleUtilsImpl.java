package org.ovirt.engine.ui.common.utils;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;

public class ConsoleUtilsImpl implements ConsoleUtils {

    private final ClientAgentType clientAgentType;
    private final Configurator configurator;

    @Inject
    public ConsoleUtilsImpl(Configurator configurator) {
        this.configurator = configurator;
        this.clientAgentType = new ClientAgentType();
    }

    @Override
    public boolean isSpiceProxyDefined() {
        return configurator.isSpiceProxyDefined();
    }

    /**
     * The ctrl+alt+del is enabled for all OS except windows newer than 7
     * @return false if and only if the client OS type is Windows 7 or newer otherwise returns true
     */
    @Override
    public boolean isCtrlAltDelEnabled() {
        if (!configurator.isClientWindows()) {
            return true;
        }

        float ntVersion = extractNtVersion(getUserAgentString());

        // For Windows 7 and Windows Server 2008 R2 it is NT 6.1
        // For Windows 8 and Windows Server 2012 it is NT 6.2
        // The passing of ctrl+alt+del is enabled only on windows older
        // than Windows 7, so NT less than 6.1
        if (ntVersion >= 6.1f) {
            return false;
        }

        return true;
    }

    private float extractNtVersion(String userAgentType) {
        RegExp pattern = RegExp.compile(".*windows nt (\\d+\\.\\d+).*"); //$NON-NLS-1$
        MatchResult matcher = pattern.exec(userAgentType.toLowerCase());
        boolean matchFound = (matcher != null);
        if (matchFound) {
            return Float.parseFloat(matcher.getGroup(1));
        }

        return -1;
    }


    @Override
    public boolean isWebSocketProxyDefined() {
        return configurator.isWebSocketProxyDefined();
    }

    @Override
    public boolean isBrowserPluginSupported(ConsoleProtocol protocol) {
        switch (protocol) {
        case SPICE:
            if ((clientAgentType.os.equalsIgnoreCase("Windows")) //$NON-NLS-1$
                    && (clientAgentType.browser.equalsIgnoreCase("Explorer")) //$NON-NLS-1$
                    && (clientAgentType.version >= 7.0)) {
                return true;
            } else if ((clientAgentType.os.equalsIgnoreCase("Linux")) //$NON-NLS-1$
                    && (clientAgentType.browser.equalsIgnoreCase("Firefox")) //$NON-NLS-1$
                    && (clientAgentType.version >= 2.0)) {
                return true;
            }
            return false;
        case RDP:
            if ((clientAgentType.os.equalsIgnoreCase("Windows"))//$NON-NLS-1$
                    && (clientAgentType.browser.equalsIgnoreCase("Explorer"))//$NON-NLS-1$
                    && (clientAgentType.version >= 7.0)) {
                return true;
            }
            return false;
        default:
            return false;
        }
    }

    private native String getUserAgentString() /*-{
                                              var userAgent = navigator.userAgent;
                                              return userAgent;
                                              }-*/;

}
