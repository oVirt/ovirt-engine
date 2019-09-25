package org.ovirt.engine.core.common.utils.ansible;

public class InventoryException extends RuntimeException {

    public InventoryException(String message, String reason) {
        super(String.format(message, reason));
    }

}
