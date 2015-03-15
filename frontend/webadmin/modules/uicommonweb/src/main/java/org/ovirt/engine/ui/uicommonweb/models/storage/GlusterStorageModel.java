package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class GlusterStorageModel extends PosixStorageModel {

    public String getConfigurationMessage() {
        return ConstantsManager.getInstance().getConstants().glusterDomainConfigurationMessage();
    }

    public GlusterStorageModel() {
        getVfsType().setTitle(""); //$NON-NLS-1$
        getVfsType().setEntity("glusterfs"); //$NON-NLS-1$
        getVfsType().setIsChangable(false);
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
        getVfsType().setIsChangable(false);
    }

    @Override
    public boolean isSupportedInVersion(Version dcVersion) {
        return (Boolean) AsyncDataProvider.getInstance()
                .getConfigValuePreConverted(ConfigurationValues.GlusterFsStorageEnabled, dcVersion.toString());
    }
}
