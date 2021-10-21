package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class EditOptionsModel extends Model {

    private final List<Field<?>> fields;

    private final EntityModel<String> publicKey = new EntityModel<>("");
    private EntityModel<Boolean> localStoragePersistedOnServer = new EntityModel<>(true);
    private EntityModel<Boolean> confirmSuspendingVm = new EntityModel<>(true);

    public EditOptionsModel(ConfirmationModelSettingsManager confirmationModelSettingsManager,
            LocalStorage localStorage) {
        this.fields = Arrays.asList(
                new PublicSshKeyField(publicKey, UserProfileProperty.builder().withDefaultSshProp().build()),
                new LocalStoragePersistenceField(localStoragePersistedOnServer, localStorage),
                new ConfirmSuspendingVmField(confirmSuspendingVm, confirmationModelSettingsManager));

        for (Field<?> field : fields) {
            field.getEntity().getEntityChangedEvent().addListener(this::updateAvailability);
        }
    }

    private void updateAvailability(Event<? extends EventArgs> ev,
            Object sender,
            EventArgs args) {
        updateAvailability();
    }

    void updateAvailability() {
        getCommands().stream()
                .filter(command -> !command.getIsCancel())
                .findFirst()
                .ifPresent(action -> action.setIsExecutionAllowed(hasChangedValues()));
    }

    public boolean hasChangedValues() {
        return fields.stream().anyMatch(Field::hasChanged);
    }

    // required to generate the view/driver
    public EntityModel<String> getPublicKey() {
        return publicKey;
    }

    public EntityModel<Boolean> getLocalStoragePersistedOnServer() {
        return localStoragePersistedOnServer;
    }

    public EntityModel<Boolean> getConfirmSuspendingVm() {
        return confirmSuspendingVm;
    }

    public void setLocalStoragePersistedOnServer(EntityModel<Boolean> localStoragePersistedOnServer) {
        this.localStoragePersistedOnServer = localStoragePersistedOnServer;
    }

    public void setConfirmSuspendingVm(EntityModel<Boolean> confirmSuspendingVm) {
        this.confirmSuspendingVm = confirmSuspendingVm;
    }

    @Override
    protected void cleanupEvents(Event<?>... events) {
        super.cleanupEvents(events);
        for (Field<?> field : fields) {
            field.getEntity().getEntityChangedEvent().clearListeners();
        }
    }

    public List<Field<?>> getFields() {
        return fields;
    }

    public List<Field<?>> getUpdates() {
        return fields.stream()
                .filter(Field::isUpdated)
                .collect(Collectors.toList());
    }

    public List<Field<?>> getRemovals() {
        return fields.stream()
                .filter(Field::isRemoved)
                .collect(Collectors.toList());
    }
}
