package org.ovirt.engine.exttool.core;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;

import java.util.List;
import java.util.Map;

public interface ModuleService {

    public static final ExtKey EXTENSIONS_MAP = new ExtKey("GLOBAL_EXTENSIONS_MAP", Map/*<ExtMap>*/.class, "78680401-40cf-4bae-a6ee-7e8c204a9010");
    public static final ExtKey PROGRAM_NAME = new ExtKey("PROGRAM_NAME", String.class, "a274d0e8-fb6e-46d5-932e-4ddb385ba0f6");

    public String getName();

    public String getDescription();

    public void setContext(ExtMap context);

    public ExtMap getContext();

    public void parseArguments(List<String> argsList) throws Exception;

    public void run() throws  Exception;

}
