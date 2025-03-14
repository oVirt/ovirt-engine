package org.ovirt.engine.ui.uicommonweb.models.options;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.LocalStorage;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModelSettingsManager;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class EditOptionsModel extends Model {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private final List<Field<?>> fields;

    private final EntityModel<String> publicKey = new EntityModel<>("");
    private EntityModel<Boolean> localStoragePersistedOnServer =
            new EntityModel<>(constants.persistGridSettingsOnServer(), true);
    private EntityModel<Boolean> confirmSuspendingVm = new EntityModel<>(constants.confirmSuspendingVm(), true);
    private final EntityModel<String> userName = new EntityModel<>("");
    private final EntityModel<String> email = new EntityModel<>("");
    private final EntityModel<Boolean> isHomePageCustom = new EntityModel<>(constants.homePageCustom(), false);
    // in the UI customHomePage(text area) shares the same label with radio button that enables it
    private final EntityModel<String> customHomePage = new EntityModel<>(constants.homePageCustom(), "");
    private final UICommand okCommand;
    private final UICommand resetCommand;

    public EditOptionsModel(ConfirmationModelSettingsManager confirmationModelSettingsManager,
            LocalStorage localStorage,
            DbUser user,
            UICommand okCommand,
            UICommand cancelCommand,
            UICommand resetCommand) {
        this.fields = Arrays.asList(
                new UseCustomHomePageField(isHomePageCustom, true),
                new CustomHomePageField(customHomePage, true),
                new PublicSshKeyField(publicKey, UserProfileProperty.builder().withDefaultSshProp().build()),
                new LocalStoragePersistenceField(localStoragePersistedOnServer, localStorage, true),
                new ConfirmSuspendingVmField(confirmSuspendingVm, confirmationModelSettingsManager, true));

        for (Field<?> field : fields) {
            field.getEntity().getEntityChangedEvent().addListener(this::updateAvailability);
        }

        userName.setEntity(user.getLoginName());
        email.setEntity(user.getEmail());

        this.okCommand = okCommand;
        this.resetCommand = resetCommand;
        // enable if values are edited
        getOkCommand().setIsExecutionAllowed(false);
        getCommands().addAll(Arrays.asList(okCommand, cancelCommand, resetCommand));

    }

    private void updateAvailability(Event<? extends EventArgs> ev,
            Object sender,
            EventArgs args) {
        updateAvailability();
    }

    void updateAvailability() {
        // Cancel is always enabled
        getOkCommand().setIsExecutionAllowed(hasChangedValues());
        getResetCommand().setIsExecutionAllowed(!hasChangedValues() && hasCustomValues());
    }

    private boolean hasCustomValues() {
        return fields.stream().filter(Field::isResettable).anyMatch(Field::isCustom);
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

    public EntityModel<String> getUserName() {
        return userName;
    }

    public EntityModel<String> getEmail() {
        return email;
    }

    public EntityModel<Boolean> getIsHomePageCustom() {
        return isHomePageCustom;
    }

    public EntityModel<String> getCustomHomePage() {
        return customHomePage;
    }

    public UICommand getOkCommand() {
        return okCommand;
    }

    public UICommand getResetCommand() {
        return resetCommand;
    }
}
