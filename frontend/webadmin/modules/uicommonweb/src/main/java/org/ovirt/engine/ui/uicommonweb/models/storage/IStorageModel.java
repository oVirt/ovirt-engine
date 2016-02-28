package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.UICommand;

@SuppressWarnings("unused")
public interface IStorageModel {
    StorageModel getContainer();

    void setContainer(StorageModel value);

    StorageType getType();

    StorageDomainType getRole();

    void setRole(StorageDomainType value);

    UICommand getUpdateCommand();

    boolean validate();

    void prepareForEdit(StorageDomain storage);

    boolean isEditable(StorageDomain storage);
}
