package org.ovirt.engine.core.common.vdscommands;

/**
 * In a VDS command parameters object, this interface
 * represents the ability to post zeros on the storage.
 */
public interface PostZero {

    boolean getPostZero();

    void setPostZero(boolean postZero);

}
