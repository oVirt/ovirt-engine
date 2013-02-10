package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LinuxMountPointValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NonUtfValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class NfsStorageModel extends Model implements IStorageModel {

    //retrans nfs option max value
    private final static short RETRANS_MAX = 32767;
    //timeo nfs option max value
    private final static short TIMEOUT_MAX = 6000;

    public static EventDefinition PathChangedEventDefinition;
    private Event pathChangedEvent;

    public Event getPathChangedEvent() {
        return pathChangedEvent;
    }

    private void setPathChangedEvent(Event value) {
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

    private EntityModel path;

    public EntityModel getPath() {
        return path;
    }

    private void setPath(EntityModel value) {
        path = value;
    }

    private EntityModel override;

    public EntityModel getOverride() {
        return override;
    }

    private void setOverride(EntityModel value) {
        override = value;
    }

    private ListModel version;

    public ListModel getVersion() {
        return version;
    }

    private void setVersion(ListModel value) {
        version = value;
    }

    private EntityModel retransmissions;

    public EntityModel getRetransmissions() {
        return retransmissions;
    }

    private void setRetransmissions(EntityModel value) {
        retransmissions = value;
    }

    private EntityModel timeout;

    public EntityModel getTimeout() {
        return timeout;
    }

    private void setTimeout(EntityModel value) {
        timeout = value;
    }


    static {

        PathChangedEventDefinition = new EventDefinition("PathChanged", NfsStorageModel.class); //$NON-NLS-1$
    }

    public NfsStorageModel() {

        setPathChangedEvent(new Event(PathChangedEventDefinition));

        setUpdateCommand(new UICommand("Update", this)); //$NON-NLS-1$

        setPath(new EntityModel());
        getPath().getEntityChangedEvent().addListener(this);

        Constants constants = ConstantsManager.getInstance().getConstants();

        // Initialize version list.
        setVersion(new ListModel());

        List<EntityModel> versionItems = new ArrayList<EntityModel>();
        // Items are shown in the UI in the order added; v3 is the default
        versionItems.add(new EntityModel(constants.nfsVersion3(), NfsVersion.V3));
        versionItems.add(new EntityModel(constants.nfsVersion4(), NfsVersion.V4));
        versionItems.add(new EntityModel(constants.nfsVersionAutoNegotiate(), NfsVersion.AUTO));
        getVersion().setItems(versionItems);

        setRetransmissions(new EntityModel());
        setTimeout(new EntityModel());

        setOverride(new EntityModel());
        getOverride().getEntityChangedEvent().addListener(this);
        getOverride().setEntity(false);

    }

    private void Override_EntityChanged(EventArgs e) {
        // Advanced options are editable only if override checkbox is enabled
        // and the dialog is not editing existing nfs storage.
        boolean isChangeable = (Boolean) getOverride().getEntity();
        getVersion().setIsChangable(isChangeable);
        getRetransmissions().setIsChangable(isChangeable);
        getTimeout().setIsChangable(isChangeable);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);
        if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getPath()) {
            // Notify about path change.
            getPathChangedEvent().raise(this, EventArgs.Empty);
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getOverride()) {
            Override_EntityChanged(args);
        }
    }

    @Override
    public boolean Validate() {
        getPath().ValidateEntity(new IValidation[] {
            new NotEmptyValidation(),
            new LinuxMountPointValidation(),
            new NonUtfValidation()
        });

        getRetransmissions().ValidateEntity(new IValidation[] {
            new IntegerValidation(0, RETRANS_MAX)
        });

        getTimeout().ValidateEntity(new IValidation[] {
            new IntegerValidation(1, TIMEOUT_MAX)
        });

        return getPath().getIsValid()
            && getRetransmissions().getIsValid()
            && getTimeout().getIsValid();
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

        ListModel dataCenter = getContainer().getDataCenter();
        dataCenter.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {

                containerDataCenterChanged();
            }
        });

        // Call handler if there some data center is already selected.
        if (dataCenter.getSelectedItem() != null) {
            containerDataCenterChanged();
        }
    }

    private void containerDataCenterChanged() {

        // Show advanced NFS options for <=3.1
        storage_pool dataCenter = (storage_pool) getContainer().getDataCenter().getSelectedItem();
        Version ver31 = new Version(3, 1);

        boolean available = dataCenter != null && (dataCenter.getcompatibility_version().compareTo(ver31) >= 0 || dataCenter.getId().equals(Guid.Empty));

        getVersion().setIsAvailable(available);
        getRetransmissions().setIsAvailable(available);
        getTimeout().setIsAvailable(available);
    }
}
