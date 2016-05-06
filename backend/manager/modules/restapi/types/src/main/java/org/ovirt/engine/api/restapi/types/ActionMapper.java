package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.CreationStatus;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;

public class ActionMapper {

    @Mapping(from = List.class, to = Action.class)
    public static Action map(List<AsyncTaskStatus> entity, Action template) {
        Action model = template != null ? template : new Action();
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
