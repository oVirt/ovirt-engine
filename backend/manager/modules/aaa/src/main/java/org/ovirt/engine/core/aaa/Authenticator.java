package org.ovirt.engine.core.aaa;

import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Extension;

/**
 * A authenticator is an object used to verify an identity.
 */
public abstract class Authenticator implements Extension {

    protected Map<Extension.ExtensionProperties, Object> context;

    /**
     * Returns the name of the profile the authenticator is associated with
     * @return profile name
     */
    public String getName() {
        return (String) context.get(ExtensionProperties.NAME);
    }

    public String getProfileName() {
        return ((Properties) context.get(ExtensionProperties.CONFIGURATION)).getProperty("ovirt.engine.aaa.authn.profile.name");
    }


    @Override
    public void setContext(Map<ExtensionProperties, Object> context) {
        this.context = context;
    }

    @Override
    public Map<ExtensionProperties, Object> getContext() {
        return context;
    }


    protected Authenticator() {
    }


}
