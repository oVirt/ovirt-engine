package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Objects;

import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public class EditOptionsModel extends Model {

    private EntityModel<String> publicKey;

    private EntityModel<Boolean> localStoragePersistedOnServer;
    private boolean sshUploadSucceeded;
    private boolean optionsUploadSucceeded;
    private EntityModel<String> originalPublicKey = new EntityModel<>();
    private EntityModel<Boolean> originalLocalStoragePersistedOnServer = new EntityModel<>();

    public EditOptionsModel() {
        setPublicKey(new EntityModel<>());
        setLocalStoragePersistedOnServer(new EntityModel<>());
        // "original" values are set earlier so
        // only work values require listeners
        getPublicKey().getEntityChangedEvent().addListener(this::updateAvailability);
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
        return !Objects.equals(originalPublicKey.getEntity(), publicKey.getEntity()) ||
                !Objects.equals(originalLocalStoragePersistedOnServer.getEntity(),
                        localStoragePersistedOnServer.getEntity());
    }

    public EntityModel<String> getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(EntityModel<String> textInput) {
        this.publicKey = textInput;
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

    public EntityModel<String> getOriginalPublicKey() {
        return originalPublicKey;
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
