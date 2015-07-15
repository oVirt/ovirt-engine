package org.ovirt.engine.exttool.core;

import java.util.List;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.core.extensions.mgr.ExtensionsManager;

public interface ModuleService {

    public static class ContextKeys {
        public static final ExtKey EXTENSION_MANAGER = new ExtKey("EXTENSION_TOOL_EXTENSION_MANAGER", ExtensionsManager/*<ExtensionsManager>*/.class, "9c718d90-3cc3-429d-9433-af5aa327267a");
        public static final ExtKey PROGRAM_NAME = new ExtKey("EXTENSION_TOOL_PROGRAM_NAME", String.class, "a274d0e8-fb6e-46d5-932e-4ddb385ba0f6");
    }

    public String getName();

    public String getDescription();

    public void setContext(ExtMap context);

    public ExtMap getContext();

    public void parseArguments(List<String> args) throws Exception;

    public void run() throws  Exception;

}
