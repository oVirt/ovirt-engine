package org.ovirt.engine.ui.uicommonweb.models.quota;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class ChangeQuotaItemModel extends EntityModel {
    EntityModel object;
    EntityModel currentQuota;
    ListModel quota;

    public ChangeQuotaItemModel() {
        object = new EntityModel();
        currentQuota = new EntityModel();
        quota = new ListModel();
    }

    public EntityModel getObject() {
        return object;
    }

    public void setObject(EntityModel object) {
        this.object = object;
    }

    public EntityModel getCurrentQuota() {
        return currentQuota;
    }

    public void setCurrentQuota(EntityModel currentQuota) {
        this.currentQuota = currentQuota;
    }

    public ListModel getQuota() {
        return quota;
    }

    public void setQuota(ListModel quota) {
        this.quota = quota;
    }

}
