package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public interface ImportParameters {
    boolean isImportAsNewEntity();
    Guid getStorageDomainId();
    Guid getStoragePoolId();
    boolean isImagesExistOnTargetStorageDomain();
    boolean getCopyCollapse();
}
