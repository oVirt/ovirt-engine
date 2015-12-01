package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ChangeQuotaItemModel extends EntityModel<DiskImage> {
    EntityModel<String> object;
    EntityModel<String> currentQuota;
    ListModel<Quota> quota;
    private Guid storageDomainId;
    private String storageDomainName;

    public ChangeQuotaItemModel() {
        object = new EntityModel<>();
        currentQuota = new EntityModel<>();
        quota = new ListModel<>();
    }

    public EntityModel<String> getObject() {
        return object;
    }

    public void setObject(EntityModel<String> object) {
        this.object = object;
    }

    public EntityModel<String> getCurrentQuota() {
        return currentQuota;
    }

    public void setCurrentQuota(EntityModel<String> currentQuota) {
        this.currentQuota = currentQuota;
    }

    public ListModel<Quota> getQuota() {
        return quota;
    }

    public void setQuota(ListModel<Quota> quota) {
        this.quota = quota;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public String getStorageDomainName() {
        return storageDomainName;
    }

    public void setStorageDomainName(String storageDomainName) {
        this.storageDomainName = storageDomainName;
    }

}
