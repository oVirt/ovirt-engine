package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;

public class ResourceManagerException extends ApplicationException implements java.io.Serializable {
    // protected ResourceManagerException(SerializationInfo info,
    // StreamingContext context)
    // {
    // super(info, context);
    // }
    //
    public ResourceManagerException(RuntimeException baseException) {
        super("ResourceManagerException", baseException);
    }

    public ResourceManagerException(String errorStr) {
        super(errorStr);

    }
}
