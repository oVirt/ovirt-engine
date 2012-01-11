package org.ovirt.engine.ui.webadmin.system;

import org.ovirt.engine.ui.common.uicommon.ClientAgentType;

import com.google.inject.Inject;

/**
 * Internal application configuration.
 */
public class InternalConfiguration {

    /**
     * Represents a browser supported by the application.
     */
    enum SupportedBrowser {

        Firefox7("Firefox", 7.0f),
        Explorer9("Explorer", 9.0f);

        private final String browser;
        private final float version;

        SupportedBrowser(String browser, float version) {
            this.browser = browser;
            this.version = version;
        }

    };

    private final ClientAgentType clientAgentType;

    @Inject
    public InternalConfiguration(ClientAgentType clientAgentType) {
        this.clientAgentType = clientAgentType;
    }

    public boolean isCurrentBrowserSupported() {
        return isBrowserSupported(getCurrentBrowser(), getCurrentBrowserVersion());
    }

    boolean isBrowserSupported(String browser, float version) {
        for (SupportedBrowser supportedBrowser : SupportedBrowser.values()) {
            if (supportedBrowser.browser.equalsIgnoreCase(browser)
                    && supportedBrowser.version == version) {
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

}
