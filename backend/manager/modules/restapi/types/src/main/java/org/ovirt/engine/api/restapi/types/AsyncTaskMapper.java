package org.ovirt.engine.api.restapi.types;

import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;

public class AsyncTaskMapper {

    @Mapping(from = AsyncTaskStatus.class, to = CreationStatus.class)
    public static CreationStatus map(AsyncTaskStatus entity, CreationStatus template) {
        CreationStatus model = null;
        switch(entity.getStatus()) {
            case unknown:
            case init:
                model = template != CreationStatus.FAILED ? CreationStatus.PENDING : template;
                break;
            case running:
                model = template != CreationStatus.FAILED ? CreationStatus.IN_PROGRESS : template;
                break;
            case finished:
                if (entity.getResult() == AsyncTaskResultEnum.success) {
                    model = template != null ? template : CreationStatus.COMPLETE;
                } else {
                    model = CreationStatus.FAILED;
                }
                break;
            case cleaning:
                if (entity.getResult() == AsyncTaskResultEnum.cleanSuccess) {
                    model = template != null ? template : CreationStatus.COMPLETE;
                } else {
                    model = CreationStatus.FAILED;
                }
                break;
            case aborting:
                model = CreationStatus.FAILED;
                break;
        }
        return model;
    }

}
