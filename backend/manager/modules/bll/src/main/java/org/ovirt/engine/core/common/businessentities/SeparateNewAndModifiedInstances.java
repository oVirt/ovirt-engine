package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.List;

public class SeparateNewAndModifiedInstances {

    private final List<BusinessEntity<?>> newEntities = new ArrayList<>();
    private final List<BusinessEntity<?>> updatedEntities = new ArrayList<>();

    public SeparateNewAndModifiedInstances(List<? extends BusinessEntity<?>> businessEntities) {
        for (BusinessEntity<?> businessEntity :  businessEntities) {
            boolean withoutId = businessEntity.getId() == null;
            if (withoutId) {
                newEntities.add(businessEntity);
            } else {
                updatedEntities.add(businessEntity);
            }
        }
    }

    public List<BusinessEntity<?>> getNewEntities() {
        return newEntities;
    }

    public List<BusinessEntity<?>> getUpdatedEntities() {
        return updatedEntities;
    }
}
