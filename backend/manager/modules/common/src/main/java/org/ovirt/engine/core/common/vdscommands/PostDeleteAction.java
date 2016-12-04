package org.ovirt.engine.core.common.vdscommands;

/**
 * In a VDS command parameters object, this interface represents the ability
 * to act upon the storage right before deleting an image or an image group.
 */
public interface PostDeleteAction {

    boolean getPostZero();

    void setPostZero(boolean postZero);

    boolean isDiscard();

    void setDiscard(boolean discard);
}
