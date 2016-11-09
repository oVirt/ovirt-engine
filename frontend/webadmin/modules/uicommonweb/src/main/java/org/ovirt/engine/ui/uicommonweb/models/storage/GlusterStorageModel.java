package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
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
        AsyncDataProvider.getInstance().getGlusterVolumesForStorageDomain(new AsyncQuery<>(new AsyncCallback<List<GlusterVolumeEntity>>() {
            @Override
            public void onSuccess(List<GlusterVolumeEntity> glusterVolumes) {
                getGlusterVolumes().setItems(glusterVolumes);
                getGlusterVolumes().setSelectedItem(null);
            }
        }));
    }

    @Override
    public StorageType getType() {
        return StorageType.GLUSTERFS;
    }

    @Override
    public VdcActionType getAddStorageDomainVdcAction() {
        return VdcActionType.AddGlusterFsStorageDomain;
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


}
