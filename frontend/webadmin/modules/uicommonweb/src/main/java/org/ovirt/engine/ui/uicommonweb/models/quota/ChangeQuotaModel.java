package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class ChangeQuotaModel extends ListModel {

    public void init(final ArrayList<DiskImage> disks) {
        final Map<Guid, List<Quota>> storageDomainIdMap = new HashMap<Guid, List<Quota>>();
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<VdcQueryParametersBase>();

        for (DiskImage diskImage : disks) {
            if (storageDomainIdMap.containsKey(diskImage.getId())) {
                continue;
            }
            storageDomainIdMap.put(diskImage.getId(), new ArrayList<Quota>());
            queryTypeList.add(VdcQueryType.GetAllRelevantQuotasForStorage);
            queryParamsList.add(new GetAllRelevantQuotasForStorageParameters(diskImage.getStorageIds().get(0)));
        }

        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, new IFrontendMultipleQueryAsyncCallback() {

            @Override
            public void Executed(FrontendMultipleQueryAsyncResult result) {
                storageDomainIdMap.clear();
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    VdcQueryReturnValue retVal = result.getReturnValues().get(i);
                    Guid storageId =
                            ((GetAllRelevantQuotasForStorageParameters) result.getParameters().get(i)).getStorageId();
                    storageDomainIdMap.put(storageId, (ArrayList<Quota>) retVal.getReturnValue());
                }
                ArrayList<ChangeQuotaItemModel> list = new ArrayList<ChangeQuotaItemModel>();
                for (DiskImage diskImage : disks) {
                    ChangeQuotaItemModel itemModel = new ChangeQuotaItemModel();
                    itemModel.setEntity(diskImage);
                    itemModel.getObject().setEntity(diskImage.getDiskAlias());
                    itemModel.getCurrentQuota().setEntity(diskImage.getQuotaName());
                    itemModel.getQuota().setItems(storageDomainIdMap.get(diskImage.getStorageIds().get(0)));
                    for (Quota quota : (ArrayList<Quota>) itemModel.getQuota().getItems()) {
                        if (!quota.getId().equals(diskImage.getQuotaId())) {
                            itemModel.getQuota().setSelectedItem(quota);
                            break;
                        }
                    }
                    list.add(itemModel);
                }
                ChangeQuotaModel.this.setItems(list);
                ChangeQuotaModel.this.StopProgress();
            }
        });
    }

    /**
     * Static utility method for change quota command availability.
     * the command is available when the selected item in the tree is DC
     * and one of the disks (shown in the tab) has quota mode != QuotaEnforcementTypeEnum.DISABLED.
     * The command is enabled (isExecutionAllowed == true) if it's available
     * and all the selected disks quota mode != QuotaEnforcementTypeEnum.DISABLED.
     * @param allDisks - model's disks (visible in page)
     * @param selectedDisks - model's selected disks
     * @param systemTreeSelectedItem
     * @param changeQuotaCommand
     */
    public static void updateChangeQuotaActionAvailability(List<Disk> allDisks,
            List<Disk> selectedDisks,
            SystemTreeItemModel systemTreeSelectedItem,
            UICommand changeQuotaCommand) {
        boolean isAvailable = true;
        boolean isExecutionAllowed = true;
        if (systemTreeSelectedItem != null
                && systemTreeSelectedItem.getType() == SystemTreeItemType.DataCenter) {
            if (selectedDisks != null && !selectedDisks.isEmpty()) {
                for (Disk diskItem : selectedDisks) {
                    if (diskItem.getDiskStorageType() != DiskStorageType.IMAGE ||
                            !(diskItem instanceof DiskImageBase) ||
                            ((DiskImageBase) diskItem).getQuotaEnforcementType() == QuotaEnforcementTypeEnum.DISABLED) {
                        isExecutionAllowed = false;
                        break;
                    }
                }
            } else {
                isExecutionAllowed = false;
            }
        } else {
            isAvailable = false;
        }
        // show the button iff there are disks with quota mode != disabled
        if (isAvailable && !isExecutionAllowed) {
            boolean hasDisksWithQuotaMode = false;
            if (allDisks != null && !allDisks.isEmpty()) {
                for (Disk diskItem : allDisks) {
                    if (diskItem.getDiskStorageType() == DiskStorageType.IMAGE &&
                            (diskItem instanceof DiskImageBase) &&
                            ((DiskImageBase) diskItem).getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
                        hasDisksWithQuotaMode = true;
                        break;
                    }
                }
            }
            isAvailable = hasDisksWithQuotaMode;
        }

        changeQuotaCommand.setIsAvailable(isAvailable);
        if (isAvailable) {
            changeQuotaCommand.setIsExecutionAllowed(isExecutionAllowed);
        }
    }
}
