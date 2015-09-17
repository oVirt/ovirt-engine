package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;


import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class RemoveStorageQosModel extends RemoveQosModel<StorageQos> {

    public RemoveStorageQosModel(ListModel<StorageQos> sourceListModel) {
        super(sourceListModel);
    }

    @Override
    public String getTitle() {
        return ConstantsManager.getInstance().getConstants().removeStorageQoSTitle();
    }

    @Override
    protected VdcQueryType getUsingEntitiesByQosIdQueryType() {
        return VdcQueryType.GetDiskProfilesByStorageQosId;
    }

    protected void handleSetMessageQueryResult(FrontendMultipleQueryAsyncResult result) {
        setHelpTag(getRemoveQosHelpTag());
        setHashName(getRemoveQosHashName());

        int index = 0;
        int numberOfTimesUsedByDiskProfiles = 0;
        ArrayList<String> list = new ArrayList<>();
        for (VdcQueryReturnValue returnValue : result.getReturnValues()) {
            List<Nameable> diskProfileEntities = returnValue.getReturnValue();

            String qosName = sourceListModel.getSelectedItems().get(index).getName();
            if (diskProfileEntities.size() == 0) {
                list.add(qosName);
            } else {
                numberOfTimesUsedByDiskProfiles += diskProfileEntities.size();
                List<String> diskProfileNames = new ArrayList<>();
                for (Nameable diskProfileEntity : diskProfileEntities) {
                    String diskProfileName = diskProfileEntity.getName();
                    diskProfileNames.add(diskProfileName);
                }

                String diskProfileNamesAsString = StringUtils.join(diskProfileNames, ", "); //$NON-NLS-1$
                list.add(ConstantsManager.getInstance().getMessages().removeStorageQoSItem(qosName, diskProfileNamesAsString));
            }
            index++;
        }

        setMessage(getRemoveQosMessage(numberOfTimesUsedByDiskProfiles));
        setItems(list);
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
