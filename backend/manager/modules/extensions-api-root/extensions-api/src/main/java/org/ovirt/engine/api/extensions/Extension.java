package org.ovirt.engine.api.extensions;

/**
 * Interface of an extension.
 */
public interface Extension {

    /**
     * Invoke operation.
     * @param input input parameters.
     * @param output output parameters.
     *
     * <p>
     * Interaction is done via the parameters.
     * Exceptions are not allowed.
     * </p>
     * <p>
     * Basic mappings available at {@link Base}.
     * </p>
     *
     * @see Base
     */
    void invoke(ExtMap input, ExtMap output);

}
