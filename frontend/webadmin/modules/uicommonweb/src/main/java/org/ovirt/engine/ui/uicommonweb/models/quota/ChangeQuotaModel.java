package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ChangeQuotaModel extends ListModel<ChangeQuotaItemModel> {

    public void init(final ArrayList<DiskImage> disks) {
        ArrayList<QueryType> queryTypeList = new ArrayList<>();
        ArrayList<QueryParametersBase> queryParamsList = new ArrayList<>();
        Set<Guid> storageDomainIdSet = new HashSet<>();
        for (DiskImage diskImage : disks) {
            for (Guid storageDomainId : diskImage.getStorageIds()) {
                storageDomainIdSet.add(storageDomainId);
            }
        }
        for (Guid storageDomainId : storageDomainIdSet) {
            queryParamsList.add(new IdQueryParameters(storageDomainId));
            queryTypeList.add(QueryType.GetAllRelevantQuotasForStorage);
        }

        Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamsList, result -> {
            Map<Guid, List<Quota>> storageDomainIdMap = new HashMap<>();
            for (int i = 0; i < result.getReturnValues().size(); i++) {
                QueryReturnValue retVal = result.getReturnValues().get(i);
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
        });
    }
}
