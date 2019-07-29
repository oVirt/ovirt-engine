package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LinuxMountPointValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NoSpacesValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NonUtfValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class NfsStorageModel extends FileStorageModel {

    //retrans nfs option max value
    private static final short RETRANS_MAX = 32767;
    //timeo nfs option max value
    private static final short TIMEOUT_MAX = 6000;

    public static final EventDefinition pathChangedEventDefinition = new EventDefinition("PathChanged", NfsStorageModel.class); //$NON-NLS-1$
    private Event<EventArgs> pathChangedEvent;

    public Event<EventArgs> getPathChangedEvent() {
        return pathChangedEvent;
    }

    private void setPathChangedEvent(Event<EventArgs> value) {
        pathChangedEvent = value;
    }

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
        if (container != value) {
            container = value;
            containerChanged();
        }
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

    @Override protected void prepareConnectionForEditing(StorageServerConnections connection) {
        getRetransmissions().setEntity(connection.getNfsRetrans());
        getTimeout().setEntity(connection.getNfsTimeo());
        getMountOptions().setEntity(connection.getMountOptions());
        getVersion().getItems().stream().filter(i -> Objects.equals(i.getEntity(), connection.getNfsVersion()))
                .findFirst().ifPresent(i -> getVersion().setSelectedItem(i));
    }

    private ListModel<EntityModel<NfsVersion>> version;

    public ListModel<EntityModel<NfsVersion>> getVersion() {
        return version;
    }

    private void setVersion(ListModel<EntityModel<NfsVersion>> value) {
        version = value;
    }

    private EntityModel<Short> retransmissions;

    public EntityModel<Short> getRetransmissions() {
        return retransmissions;
    }

    private void setRetransmissions(EntityModel<Short> value) {
        retransmissions = value;
    }

    private EntityModel<Short> timeout;

    public EntityModel<Short> getTimeout() {
        return timeout;
    }

    private void setTimeout(EntityModel<Short> value) {
        timeout = value;
    }

    private EntityModel<String> mountOptions;

    public EntityModel<String> getMountOptions() {
        return mountOptions;
    }

    private void setMountOptions(EntityModel<String> value) {
        mountOptions = value;
    }

    public NfsStorageModel() {
        setPathChangedEvent(new Event<>(pathChangedEventDefinition));

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$

        setPath(new EntityModel<>());
        getPath().getEntityChangedEvent().addListener(this);

        UIConstants constants = ConstantsManager.getInstance().getConstants();

        // Initialize version list.
        setVersion(new ListModel<>());

        List<EntityModel<NfsVersion>> versionItems = new ArrayList<>();
        // Items are shown in the UI in the order added; Auto negotiate is the default

        versionItems.add(new EntityModel<>(constants.nfsVersionAutoNegotiate(), NfsVersion.AUTO));
        versionItems.add(new EntityModel<>(constants.nfsVersion3(), NfsVersion.V3));
        versionItems.add(new EntityModel<>(constants.nfsVersion4(), NfsVersion.V4));
        versionItems.add(new EntityModel<>(constants.nfsVersion40(), NfsVersion.V4_0));
        versionItems.add(new EntityModel<>(constants.nfsVersion41(), NfsVersion.V4_1));
        versionItems.add(new EntityModel<>(constants.nfsVersion42(), NfsVersion.V4_2));
        getVersion().setItems(versionItems);

        setRetransmissions(new EntityModel<>());
        setTimeout(new EntityModel<>());
        setMountOptions(new EntityModel<>());

    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);
        if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition) && sender == getPath()) {
            // Notify about path change.
            getPathChangedEvent().raise(this, EventArgs.EMPTY);
        }
    }

    @Override
    public boolean validate() {
        getPath().validateEntity(new IValidation[] {
            new NotEmptyValidation(),
            new LinuxMountPointValidation(),
            new NonUtfValidation(),
            new NoSpacesValidation()
        });

        if (getRetransmissions().getIsValid()) {
            getRetransmissions().validateEntity(new IValidation[] {
                    new IntegerValidation(0, RETRANS_MAX)
            });
        }

        if (getTimeout().getIsValid()) {
            getTimeout().validateEntity(new IValidation[] {
                    new IntegerValidation(1, TIMEOUT_MAX)
            });
        }

        getMountOptions().validateEntity(new IValidation[] {
            new NonUtfValidation()
        });

        return getPath().getIsValid()
            && getRetransmissions().getIsValid()
            && getTimeout().getIsValid()
            && getMountOptions().getIsValid();
    }

    @Override
    public StorageType getType() {
        return StorageType.NFS;
    }

    private void containerChanged() {
        // Subscribe to the data center change.
        if (getContainer() == null) {
            return;
        }

        ListModel<StoragePool> dataCenter = getContainer().getDataCenter();
        dataCenter.getSelectedItemChangedEvent().addListener((ev, sender, args) -> containerDataCenterChanged());

        // Call handler if there some data center is already selected.
        if (dataCenter.getSelectedItem() != null) {
            containerDataCenterChanged();
        }
    }

    private void containerDataCenterChanged() {
        StoragePool dataCenter = getContainer().getDataCenter().getSelectedItem();
        boolean available = dataCenter != null;

        getVersion().setIsAvailable(available);
        getRetransmissions().setIsAvailable(available);
        getTimeout().setIsAvailable(available);
        getMountOptions().setIsAvailable(available);
    }

    @Override
    public void prepareForEdit(StorageDomain storage) {
        super.prepareForEdit(storage);
        boolean isStorageEditable = isEditable(storage);
        getVersion().setIsChangeable(isStorageEditable);
        getRetransmissions().setIsChangeable(isStorageEditable);
        getTimeout().setIsChangeable(isStorageEditable);
        getMountOptions().setIsChangeable(isStorageEditable);
    }

    @Override
    public void cleanup() {
        cleanupEvents(getPathChangedEvent());
        super.cleanup();
    }
}
