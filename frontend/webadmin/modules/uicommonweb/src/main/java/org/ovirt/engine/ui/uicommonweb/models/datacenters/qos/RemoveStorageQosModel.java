package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;


import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveStorageQosModel extends RemoveQosModel<StorageQos> {

    public RemoveStorageQosModel(ListModel<StorageQos> sourceListModel) {
        super(sourceListModel);
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().removeStorageQoSTitle();
    }

    @Override
    protected VdcQueryType getProfilesByQosIdQueryType() {
        return VdcQueryType.GetDiskProfilesByStorageQosId;
    }

    @Override
    protected String getRemoveQosMessage(int size) {
        return ConstantsManager.getInstance().getMessages().removeStorageQoSMessage(size);
    }

    @Override
    protected String getRemoveQosHashName() {
        return "remove_storage_qos"; //$NON-NLS-1$
    }

    @Override
    protected HelpTag getRemoveQosHelpTag() {
        return HelpTag.remove_storage_qos;
    }

    @Override
    protected VdcActionType getRemoveActionType() {
        return VdcActionType.RemoveStorageQos;
    }

}
