package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class GlusterStorageModel extends PosixStorageModel {

    private EntityModel<Boolean> linkGlusterVolume;
    private ListModel<GlusterVolumeEntity> glusterVolumes;

    public String getConfigurationMessage() {
        return ConstantsManager.getInstance().getConstants().glusterDomainConfigurationMessage();
    }

    public GlusterStorageModel() {
        setLinkGlusterVolume(new EntityModel<Boolean>(false));
        setGlusterVolumes(new ListModel<GlusterVolumeEntity>());
        getVfsType().setTitle(""); //$NON-NLS-1$
        getVfsType().setEntity("glusterfs"); //$NON-NLS-1$
        getVfsType().setIsChangeable(false);
        AsyncDataProvider.getInstance().getGlusterVolumesForStorageDomain(new AsyncQuery<>(glusterVolumes -> {
            getGlusterVolumes().setItems(glusterVolumes);
            getGlusterVolumes().setSelectedItem(null);
        }));
        getGlusterVolumes().getSelectedItemChangedEvent().addListener((ev, sender, args) -> volumeSelectedItemChanged());
        getLinkGlusterVolume().getEntityChangedEvent().addListener((ev, sender, args) -> volumeSelectedItemChanged());
    }

    private void volumeSelectedItemChanged() {
        GlusterVolumeEntity volume = getGlusterVolumes().getSelectedItem();
        Boolean useLinkGlusterVolume = getLinkGlusterVolume().getEntity();
        String mountOptions = ""; //$NON-NLS-1$
        if (volume == null || !useLinkGlusterVolume) {
            return;
        }
        Set<String> addressSet = new LinkedHashSet<>();
        for (GlusterBrickEntity brick : volume.getBricks()) {
            addressSet.add(brick.getNetworkId() != null && !brick.getNetworkAddress().isEmpty()
                    ? brick.getNetworkAddress() : brick.getServerName());
        }
        List<String> addressList = new ArrayList<>();
        addressList.addAll(addressSet);
        if (addressList.size() >= 1) {
            // the first server is already used to mount volume
            addressList.remove(0);
        }
        if (addressList.size() > 0) {
            mountOptions = StorageConstants.GLUSTER_BACKUP_SERVERS_MNT_OPTION
                    + "=" + String.join(":", addressList); //$NON-NLS-1$ //$NON-NLS-2$
        }
        getMountOptions().setEntity(mountOptions);
    }


    @Override
    public StorageType getType() {
        return StorageType.GLUSTERFS;
    }

    @Override
    public ActionType getAddStorageDomainVdcAction() {
        return ActionType.AddGlusterFsStorageDomain;
    }

    @Override
    public void setVfsChangeability(boolean isVfsChangeable) {
        getVfsType().setIsChangeable(false);
    }

    public EntityModel<Boolean> getLinkGlusterVolume() {
        return linkGlusterVolume;
    }

    public void setLinkGlusterVolume(EntityModel<Boolean> linkGlusterVolume) {
        this.linkGlusterVolume = linkGlusterVolume;
    }

    public ListModel<GlusterVolumeEntity> getGlusterVolumes() {
        return glusterVolumes;
    }

    public void setGlusterVolumes(ListModel<GlusterVolumeEntity> glusterVolumes) {
        this.glusterVolumes = glusterVolumes;
    }

    @Override
    public boolean validate() {
        getGlusterVolumes().validateSelectedItem(new IValidation[] { new NotEmptyValidation(), new LengthValidation(128)});
        getVfsType().validateEntity(
                new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() }
        );

        if (getLinkGlusterVolume().getEntity()) {
            return getGlusterVolumes().getIsValid() && getVfsType().getIsValid();
        } else {
            return super.validate();
        }
    }

    @Override
    protected void prepareConnectionForEditing(StorageServerConnections connection) {
        super.prepareConnectionForEditing(connection);
        getLinkGlusterVolume().setEntity(connection.getGlusterVolumeId() != null);
    }

    @Override
    public void prepareForEdit(StorageDomain storage) {
        super.prepareForEdit(storage);
        boolean isEditable = isEditable(storage);
        getLinkGlusterVolume().setIsChangeable(isEditable);
    }



}
