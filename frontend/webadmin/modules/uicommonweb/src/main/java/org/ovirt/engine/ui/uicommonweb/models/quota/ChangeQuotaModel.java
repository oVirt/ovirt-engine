package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
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

public class ChangeQuotaModel extends ListModel<ChangeQuotaItemModel> {

    public void init(final ArrayList<DiskImage> disks) {
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        Set<Guid> storageDomainIdSet = new HashSet<>();
        for (DiskImage diskImage : disks) {
            for (Guid storageDomainId : diskImage.getStorageIds()) {
                storageDomainIdSet.add(storageDomainId);
            }
        }
        for (Guid storageDomainId : storageDomainIdSet) {
            queryParamsList.add(new IdQueryParameters(storageDomainId));
            queryTypeList.add(VdcQueryType.GetAllRelevantQuotasForStorage);
        }

        Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamsList, new IFrontendMultipleQueryAsyncCallback() {

            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {
                Map<Guid, List<Quota>> storageDomainIdMap = new HashMap<>();
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    VdcQueryReturnValue retVal = result.getReturnValues().get(i);
                    Guid storageId =
                            ((IdQueryParameters) result.getParameters().get(i)).getId();
                    storageDomainIdMap.put(storageId, (ArrayList<Quota>) retVal.getReturnValue());
                }
                ArrayList<ChangeQuotaItemModel> list = new ArrayList<>();
                Guid storageDomainId;
                for (DiskImage diskImage : disks) {
                    for (int i = 0; i < diskImage.getStorageIds().size(); i++) {
                        storageDomainId = diskImage.getStorageIds().get(i);
                        ChangeQuotaItemModel itemModel = new ChangeQuotaItemModel();
                        itemModel.setEntity(diskImage);
                        itemModel.getObject().setEntity(diskImage.getDiskAlias());
                        itemModel.getCurrentQuota().setEntity(diskImage.getQuotaNames() != null && diskImage.getQuotaNames().size() >= i+1 ? diskImage.getQuotaNames().get(i) : null);
                        itemModel.setStorageDomainId(storageDomainId);
                        itemModel.setStorageDomainName(diskImage.getStoragesNames().get(i));
                        itemModel.getQuota().setItems(storageDomainIdMap.get(storageDomainId));
                        for (Quota quota : itemModel.getQuota().getItems()) {
                            if (!quota.getId().equals(diskImage.getQuotaId())) {
                                itemModel.getQuota().setSelectedItem(quota);
                                break;
                            }
                        }
                        list.add(itemModel);
                    }
                }
                ChangeQuotaModel.this.setItems(list);
                ChangeQuotaModel.this.stopProgress();
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
     */
    public static void updateChangeQuotaActionAvailability(Collection<? extends Disk> allDisks,
            Collection<? extends Disk> selectedDisks,
            SystemTreeItemModel systemTreeSelectedItem,
            UICommand changeQuotaCommand) {
        boolean isAvailable = true;
        boolean isExecutionAllowed = true;
        if (systemTreeSelectedItem != null
                && systemTreeSelectedItem.getType() == SystemTreeItemType.DataCenter) {
            if (selectedDisks != null && !selectedDisks.isEmpty()) {
                for (Disk diskItem : selectedDisks) {
                    if (!diskItem.getDiskStorageType().isInternal() ||
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
                    if (diskItem.getDiskStorageType().isInternal() &&
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
