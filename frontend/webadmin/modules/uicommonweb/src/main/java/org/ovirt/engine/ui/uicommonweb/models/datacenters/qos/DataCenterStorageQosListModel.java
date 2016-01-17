package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DataCenterStorageQosListModel extends DataCenterQosListModel<StorageQos, StorageQosParametersModel> {
    @Override
    protected String getQosTitle() {
        return ConstantsManager.getInstance().getConstants().storageQosTitle();
    }

    @Override
    protected String getQosHashName() {
        return "storage_qos"; //$NON-NLS-1$
    }

    @Override
    protected HelpTag getQosHelpTag() {
        return HelpTag.storage_qos;
    }

    @Override
    protected QosType getQosType() {
        return QosType.STORAGE;
    }

    @Override
    protected QosModel<StorageQos, StorageQosParametersModel> getNewQosModel() {
        return new NewStorageQosModel(this, getEntity());
    }

    @Override
    protected QosModel<StorageQos, StorageQosParametersModel> getEditQosModel(StorageQos qoS) {
        return new EditStorageQosModel(getSelectedItem(), this, getEntity());
    }

    @Override
    protected RemoveQosModel<StorageQos> getRemoveQosModel() {
        return new RemoveStorageQosModel(this);
    }

    @Override
    protected String getListName() {
        return "DataCenterStorageQosModel"; //$NON-NLS-1$
    }

}
