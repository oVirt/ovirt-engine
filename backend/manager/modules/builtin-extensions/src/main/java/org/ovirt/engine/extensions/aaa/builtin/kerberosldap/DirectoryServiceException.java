package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

/**
 * Base class for engine DS exceptions
 *
 */
public class DirectoryServiceException extends RuntimeException {

    public DirectoryServiceException() {
        // TODO Auto-generated constructor stub
    }

    public DirectoryServiceException(String message) {
        super(message);
    }

    public DirectoryServiceException(Throwable cause) {
        super(cause);
    }

    public DirectoryServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
