package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class GlusterStorageModel extends PosixStorageModel {

    public String getConfigurationMessage() {
        return ConstantsManager.getInstance().getConstants().glusterDomainConfigurationMessage();
    }

    public GlusterStorageModel() {
        getVfsType().setTitle(""); //$NON-NLS-1$
        getVfsType().setEntity("glusterfs"); //$NON-NLS-1$
        getVfsType().setIsChangeable(false);
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
}
