package org.ovirt.engine.core.dal.dbbroker.generic;

import org.ovirt.engine.core.compat.*;

/**
 * This class is the base class for all exceptions from our repositories.
 */
public class RepositoryException extends ApplicationException implements java.io.Serializable {
    private static final long serialVersionUID = -8256930086206446087L;
    // public RepositoryException()
    // {
    // }
    //
    // public RepositoryException(String message)
    // {
    // super(message);
    // }
    //
    // public RepositoryException(String message, RuntimeException inner)
    // {
    // super(message, inner);
    // }
    //
    // protected RepositoryException(SerializationInfo info, StreamingContext
    // context)
    // {
    // super(info, context);
    // }
    // private static LogCompat log =
    // LogFactoryCompat.getLog(RepositoryException.class);
}
