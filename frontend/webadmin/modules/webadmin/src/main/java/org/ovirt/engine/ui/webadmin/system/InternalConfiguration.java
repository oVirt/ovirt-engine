package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.ui.common.uicommon.ClientAgentType;

import com.google.inject.Inject;

/**
 * Internal application configuration.
 */
public class InternalConfiguration {

    /**
     * Represents a browser fully supported by the application.
     */
    enum SupportedBrowser {

        // Firefox 31 on Linux
        Firefox31OnLinux("Firefox", 31.0f, false, "Linux"), //$NON-NLS-1$ //$NON-NLS-2$

        // Explorer 9+ on any OS
        Explorer9AndAbove("Explorer", 9.0f, true); //$NON-NLS-1$

        // Browser identity
        private final String browser;

        // Supported version range (closed interval)
        private final float versionFrom;
        private final float versionTo;

        // OS identity (null value disables OS check)
        private final String os;

        SupportedBrowser(String browser, float versionFrom, float versionTo, String os) {
            assert browser != null : "Browser identity cannot be null"; //$NON-NLS-1$
            this.browser = browser;
            this.versionFrom = versionFrom;
            this.versionTo = versionTo;
            this.os = os;
        }

        SupportedBrowser(String browser, float version, boolean supportVersionsAbove, String os) {
            this(browser, version, supportVersionsAbove ? Float.MAX_VALUE : version, os);
        }

        SupportedBrowser(String browser, float version, boolean supportVersionsAbove) {
            this(browser, version, supportVersionsAbove, null);
        }

        boolean browserMatches(String actualBrowser) {
            return browser.equalsIgnoreCase(actualBrowser);
        }

        boolean versionMatches(float actualVersion) {
            return versionFrom <= actualVersion && actualVersion <= versionTo;
        }

        boolean osMatches(String actualOs) {
            return os != null ? os.equalsIgnoreCase(actualOs) : true;
        }

    }

    private final ClientAgentType clientAgentType;

    @Inject
    public InternalConfiguration(ClientAgentType clientAgentType) {
        this.clientAgentType = clientAgentType;
    }

    /**
     * @return {@code true} if the current browser is fully supported by the application, {@code false} otherwise.
     */
    public boolean isCurrentBrowserSupported() {
        return isBrowserSupported(getCurrentBrowser(), getCurrentBrowserVersion(), getCurrentOs());
    }

    boolean isBrowserSupported(String browser, float version, String os) {
        for (SupportedBrowser supportedBrowser : SupportedBrowser.values()) {
            if (supportedBrowser.browserMatches(browser)
                    && supportedBrowser.versionMatches(version)
                    && supportedBrowser.osMatches(os)) {
                return true;
            }
        }

        return false;
    }

    public String getCurrentBrowser() {
        return clientAgentType.browser;
    }

    public float getCurrentBrowserVersion() {
        return clientAgentType.version;
    }

    public String getCurrentOs() {
        return clientAgentType.os;
    }

}
