package org.ovirt.engine.core.common.errors;

public enum ErrorType {

    /**
     * Client passed invalid parameters in the request. For example - a necessary parameter is missing (null), bad ID
     * supplied (e.g: moving disk to a different storage-domain - ID of the storage-domain is wrong or malformed), some
     * combination of parameters is not supported, a list which is expected to include at least one element is empty,
     * client tried to update a non-updatable property, and things like: an export domain was given where a data domain
     * is expected, etc. This request would never be legal.
     */
    BAD_PARAMETERS,

    /**
     * Client made a request which is not applicable in the current status of the engine. The same request, under
     * different circumstances, would be applicable. For example, a client wants to start a VM which is already running.
     * In other circumstances (VM down) this request would be applicable.
     */
    CONFLICT,

    /**
     * Client parameters directly violate a constraint. For example, setting VM memory of 500GB would violate the
     * constraint of maximum VM memory (even if the maximum is configurable in Vdc_Options table, we consider it a
     * constraint violation). This is different from CONFLICT, because (taking vdc_options as a given) under no
     * circumstances would it be ok to give a VM 500GB memory.
     */
    CONSTRAINT_VIOLATION,

    INCOMPATIBLE_VERSION,

    INTERNAL_ERROR,

    NOT_SUPPORTED,

    /**
     * Client failed to authenticate.
     */
    NO_AUTHENTICATION,

    /**
     * Client lacks permission to perform this operation (after authentication).
     */
    NO_PERMISSION,

    /**
     * An illegal state in the server does not enable the request to be executed. This means internal data corruption
     * had occured.
     */
    DATA_CORRUPTION,

    ATTESTATION_SERVER_ERROR,
}
