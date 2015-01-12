package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.configure.PermissionListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import com.google.inject.Inject;

public class DiskProfileListModel extends ProfileListModel<DiskProfile, StorageQos, StorageDomain> {

    @Inject
    public DiskProfileListModel(final PermissionListModel<DiskProfileListModel> permissionListModel) {
        super(permissionListModel);
        setTitle(ConstantsManager.getInstance().getConstants().diskProfilesTitle());
        setHelpTag(HelpTag.disk_profiles);
        setHashName("disk_profiles"); //$NON-NLS-1$
    }

    @Override
    protected ProfileBaseModel<DiskProfile, StorageQos, StorageDomain> getNewProfileModel() {
        return new NewDiskProfileModel(this, getStoragePoolId());
    }

    @Override
    protected ProfileBaseModel<DiskProfile, StorageQos, StorageDomain> getEditProfileModel() {
        return new EditDiskProfileModel(this, (DiskProfile) getSelectedItem(), getStoragePoolId());
    }

    @Override
    protected RemoveProfileModel<DiskProfile> getRemoveProfileModel() {
        return new RemoveDiskProfileModel(this, getSelectedItems());
    }

    @Override
    protected QosType getQosType() {
        return QosType.STORAGE;
    }

    @Override
    protected StorageDomain getParentEntity() {
        return (StorageDomain) ((super.getEntity() instanceof StorageDomain) ? super.getEntity() : null);
    }

    @Override
    protected Guid getStoragePoolId() {
        return getParentEntity() != null ? getParentEntity().getStoragePoolId() : null;
    }

    @Override
    protected VdcQueryType getQueryType() {
        return VdcQueryType.GetDiskProfilesByStorageDomainId;
    }

    @Override
    protected String getListName() {
        return "DiskProfileListModel"; //$NON-NLS-1$
    }
}
