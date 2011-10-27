package org.ovirt.engine.ui.webadmin.system;

import java.util.HashMap;
import java.util.Map;

/**
 * Internal application configurations
 */
public class InternalConfiguration {
    private Map<String, Float[]> supportedBrowsers;

    // TODO: Should be available in ClientAgentType
    enum supportedBrowsersOptions {
        Firefox,
        Explorer
    };

    public InternalConfiguration() {
        initSupportedBrowsers();
    }

    private void initSupportedBrowsers() {
        if (supportedBrowsers == null)
            supportedBrowsers = new HashMap<String,Float[]>();
        
        //Add supported browsers here
        supportedBrowsers.put(supportedBrowsersOptions.Firefox.name(),new Float[]{7.0F});
        supportedBrowsers.put(supportedBrowsersOptions.Explorer.name(),new Float[]{9.0F});
    }
    
    public Map<String, Float[]> getSupportedBrowsers() {
        return supportedBrowsers;
    }
}
