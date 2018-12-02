package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.Map;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class ManagedBlockStorageModel extends Model implements IStorageModel {

    private UICommand updateCommand;

    @Override
    public UICommand getUpdateCommand() {
        return updateCommand;
    }

    private void setUpdateCommand(UICommand value) {
        updateCommand = value;
    }

    private StorageModel container;

    @Override
    public StorageModel getContainer() {
        return container;
    }

    @Override
    public void setContainer(StorageModel value) {
        container = value;
    }

    private StorageDomainType role = StorageDomainType.values()[0];

    @Override
    public StorageDomainType getRole() {
        return role;
    }

    @Override
    public void setRole(StorageDomainType value) {
        role = value;
    }

    private KeyValueModel driverOptions;

    public KeyValueModel getDriverOptions() {
        return driverOptions;
    }

    public void setDriverOptions(KeyValueModel driverOptions) {
        this.driverOptions = driverOptions;
    }

    private KeyValueModel driverSensitiveOptions;

    public KeyValueModel getDriverSensitiveOptions() {
        return driverSensitiveOptions;
    }

    public void setDriverSensitiveOptions(KeyValueModel driverSensitiveOptions) {
        this.driverSensitiveOptions = driverSensitiveOptions;
    }

    public ManagedBlockStorageModel() {

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$
        setDriverOptions(new KeyValueModel());
        setDriverSensitiveOptions(new KeyValueModel());

        getDriverOptions().setTitle(ConstantsManager.getInstance().getConstants().driverOptionsHint());
        getDriverSensitiveOptions().setTitle(ConstantsManager.getInstance().getConstants().driverSensitiveOptionsHint());
        getDriverOptions().useEditableKey(true);
        getDriverSensitiveOptions().useEditableKey(true);
        getDriverSensitiveOptions().setMaskValueField(true);
    }

    @Override
    public boolean validate() {
        getDriverOptions().validate();
        getDriverSensitiveOptions().validate();
        return getDriverOptions().getIsValid() && getDriverSensitiveOptions().getIsValid();
    }

    @Override
    public StorageType getType() {
        return StorageType.MANAGED_BLOCK_STORAGE;
    }

    public ActionType getAddStorageDomainVdcAction() {
        return ActionType.AddManagedBlockStorageDomain;
    }

    public boolean isEditable(StorageDomain storage) {
        return storage.getStatus() == StorageDomainStatus.Maintenance
                || storage.getStorageDomainSharedStatus() == StorageDomainSharedStatus.Unattached;
    }

    @Override
    public void prepareForEdit(StorageDomain storage) {
        boolean isEditable = isEditable(storage);

        AsyncDataProvider.getInstance().getManagedBlockStorageDomainById(new AsyncQuery<>(managedBlockStorage ->
            prepareDriverOptionsForEditing(managedBlockStorage.getDriverOptions(), managedBlockStorage.getDriverSensitiveOptions())
        ), storage.getId());

        getContainer().getHost().setIsChangeable(isEditable);
        getDriverOptions().setIsChangeable(isEditable);
        getDriverSensitiveOptions().setIsChangeable(isEditable);
    }

    private void prepareDriverOptionsForEditing(Map<String, Object> driverOptionsMap, Map<String, Object> driverSensitiveOptionsMap) {
        driverOptions.createLineModelsFromMap(driverOptionsMap);
        driverSensitiveOptions.createLineModelsFromMap(driverSensitiveOptionsMap);
    }

}
