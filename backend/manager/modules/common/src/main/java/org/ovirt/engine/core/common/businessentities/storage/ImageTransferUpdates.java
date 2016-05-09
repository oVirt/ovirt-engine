package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

/**
 * Encapsulates additional metadata required for updating the ImageTransfer
 * entity, as needed.
 */
public class ImageTransferUpdates extends ImageTransfer {

    private static final long serialVersionUID = -782252905388032338L;
    boolean clearResourceId;

    public ImageTransferUpdates(Guid commandId) {
        super(commandId);
    }

    public ImageTransferUpdates() {
    }

    public boolean isClearResourceId() {
        return clearResourceId;
    }

    public void setClearResourceId(boolean clearResourceId) {
        this.clearResourceId = clearResourceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImageTransferUpdates other = (ImageTransferUpdates) obj;
        return super.equals(obj)
                && Objects.equals(clearResourceId, other.clearResourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                clearResourceId
        );
    }
}
