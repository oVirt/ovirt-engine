package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.ovirt.engine.api.model.Creation;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;

public class CreationMapper {

    @Mapping(from = List.class, to = Creation.class)
    public static Creation map(List<AsyncTaskStatus> entity, Creation template) {
        Creation model = template != null ? template : new Creation();
        CreationStatus asyncStatus = null;
        for (AsyncTaskStatus task : entity) {
            asyncStatus = AsyncTaskMapper.map(task, asyncStatus);
        }
        model.setStatus(asyncStatus.value());
        if (asyncStatus == CreationStatus.FAILED) {
            model.setFault(new Fault());
            for (AsyncTaskStatus task : entity) {
                if (task.getException() != null) {
                    model.getFault().setDetail(task.getException().toString());
                    break;
                }
            }
        }
        return model;
    }
}
