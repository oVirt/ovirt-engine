package org.ovirt.engine.core.common.vdscommands;

/**
 * Provides a way to use response processing code.
 *
 */
public interface AsyncCallback {

    /**
     * Processes response from vdsm which is represented by returnValue.
     *
     * @param returnValue {@link Map} containing response from vdsm.
     * @param value {@link VDSAsyncReturnValue} used for setting whether the processing
     *              finished successfully or not.
     * @return Business object containing processed response information.
     */
    public Object process(Object returnValue, VDSAsyncReturnValue value);
}
