package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.UserProfileProperty;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class EditOptionsModel extends Model {

    private final EntityModel<String> publicKey = new EntityModel<>("");

    private EntityModel<Boolean> localStoragePersistedOnServer = new EntityModel<>();
    private boolean sshUploadSucceeded;
    private boolean optionsUploadSucceeded;
    private final EntityModel<String> originalPublicKey = new EntityModel<>("");
    private final EntityModel<Boolean> originalLocalStoragePersistedOnServer = new EntityModel<>();

    public EditOptionsModel() {
        publicKey.getEntityChangedEvent().addListener(this::updateAvailability);
        getLocalStoragePersistedOnServer().getEntityChangedEvent().addListener(this::updateAvailability);
    }

    private void updateAvailability(Event<? extends EventArgs> ev,
            Object sender, EventArgs args) {
        getCommands().stream()
                .filter(command -> !command.getIsCancel())
                .findFirst()
                .ifPresent(action -> action.setIsExecutionAllowed(hasChangedValues()));
    }

    private boolean hasChangedValues() {
        return isSshKeyUpdated() || isSshKeyRemoved() ||
                !Objects.equals(originalLocalStoragePersistedOnServer.getEntity(),
                        localStoragePersistedOnServer.getEntity());
    }

    public boolean isSshKeyUpdated() {
        return !Objects.equals(getOriginalPublicKey(), getNewPublicKey())
                && !getNewPublicKey().isEmpty();
    }

    public boolean isSshKeyRemoved() {
        return !Objects.equals(getOriginalPublicKey(), getNewPublicKey())
                && !getOriginalPublicKey().isEmpty()
                && getNewPublicKey().isEmpty();
    }

    public String getNewPublicKey() {
        return Optional.ofNullable(publicKey.getEntity()).orElse("").trim();
    }

    // required to generate the view/driver
    public EntityModel<String> getPublicKey() {
        return publicKey;
    }

    public EntityModel<Boolean> getLocalStoragePersistedOnServer() {
        return localStoragePersistedOnServer;
    }

    public void setLocalStoragePersistedOnServer(EntityModel<Boolean> localStoragePersistedOnServer) {
        this.localStoragePersistedOnServer = localStoragePersistedOnServer;
    }

    public void setSshUploadSucceeded(boolean succeeded) {
        sshUploadSucceeded = succeeded;
    }

    public void setOptionsUploadSucceeded(boolean succeeded) {
        optionsUploadSucceeded = succeeded;
    }

    public boolean isUploadComplete() {
        return optionsUploadSucceeded && sshUploadSucceeded;
    }

    public String getOriginalPublicKey() {
        return originalPublicKey.getEntity().trim();
    }

    public void setOriginalPublicKey(String textInput) {
        if (textInput != null) {
            // "original" values are set earlier so
            // only work values require listeners
            this.originalPublicKey.setEntity(textInput);
            this.publicKey.setEntity(textInput);
        }
    }

    public void setOriginalPublicKey(UserProfileProperty prop) {
        if(prop != null) {
            setOriginalPublicKey(prop.getContent());
        }
    }

    public EntityModel<Boolean> getOriginalStoragePersistedOnServer() {
        return originalLocalStoragePersistedOnServer;
    }

    @Override
    protected void cleanupEvents(Event<?>... events) {
        super.cleanupEvents(events);
        publicKey.getEntityChangedEvent().clearListeners();
        localStoragePersistedOnServer.getEntityChangedEvent().clearListeners();
    }
}
