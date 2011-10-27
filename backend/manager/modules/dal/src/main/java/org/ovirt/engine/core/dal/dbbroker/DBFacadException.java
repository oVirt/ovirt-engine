package org.ovirt.engine.core.dal.dbbroker;

import org.ovirt.engine.core.compat.*;

public class DBFacadException extends ApplicationException implements java.io.Serializable {
    private static final long serialVersionUID = -1759699263394287948L;

    // protected DBFacadException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info,context);
    // }
    //
    public DBFacadException() {
    }

    public DBFacadException(String errorStr, Throwable cause) {
        super("DBFacadException: " + errorStr, cause);
    }
}
