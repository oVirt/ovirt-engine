package org.ovirt.engine.core.aaa;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.core.extensions.mgr.ExtensionProxy;

public class AuthzUtils {

    public static String getName(ExtensionProxy proxy) {
        return proxy.getContext().<String> get(Base.ContextKeys.INSTANCE_NAME);
    }
}
