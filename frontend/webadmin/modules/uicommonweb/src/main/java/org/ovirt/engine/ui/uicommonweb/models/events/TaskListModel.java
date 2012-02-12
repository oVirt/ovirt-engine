package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.queries.GetJobByJobIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetJobsByOffsetQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class TaskListModel extends SearchableListModel {

    private final Map<Guid, Job> detailTaskGuids = new HashMap<Guid, Job>();

    public TaskListModel() {
    }

    @Override
    protected void SyncSearch() {
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                TaskListModel taskListModel = (TaskListModel) model;
                ArrayList<Job> taskList =
                        (java.util.ArrayList<Job>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                ArrayList<Guid> removeTaskGuids = new ArrayList<Guid>();
                ArrayList<Job> newTaskList = new ArrayList<Job>();
                for (Job task : taskList) {
                    if (!detailTaskGuids.containsKey(task.getId())) {
                        removeTaskGuids.add(task.getId());
                    } else if (task.getLastUpdateTime().equals(detailTaskGuids.get(task.getId()).getLastUpdateTime())) {
                        task.setSteps(detailTaskGuids.get(task.getId()).getSteps());
                    } else {
                        detailTaskGuids.remove(task.getId());
                        updateSingleTask(task.getId());
                    }
                    newTaskList.add(task);

                }
                for (Guid guid : removeTaskGuids) {
                    detailTaskGuids.remove(guid);
                }

                taskListModel.setItems(newTaskList);
            }
        };
        GetJobsByOffsetQueryParameters tempVar = new GetJobsByOffsetQueryParameters();
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetJobsByOffset,
                tempVar, _asyncQuery);
    }

    @Override
    protected String getListName() {
        return "TaskListModel";
    }

    public boolean updateSingleTask(Guid guid) {
        if (!detailTaskGuids.containsKey(guid)) {
            detailTaskGuids.put(guid, null);
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);
            _asyncQuery.asyncCallback = new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object ReturnValue)
                {
                    TaskListModel taskListModel = (TaskListModel) model;
                    Job retTask = (Job) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                    detailTaskGuids.put(retTask.getId(), retTask);
                    ArrayList<Job> taskList = (ArrayList<Job>) taskListModel.getItems();
                    Job destTask = null;
                    int i = -1;
                    for (Job task : taskList) {
                        i++;
                        if (task.getId().equals(retTask.getId())) {
                            destTask = task;

                        }
                    }
                    if (destTask != null) {
                        taskList.remove(destTask);
                        taskList.add(i, retTask);
                    }

                    taskListModel.setItems(taskList);
                }
            };
            GetJobByJobIdQueryParameters parameters = new GetJobByJobIdQueryParameters();
            parameters.setJobId(guid);
            Frontend.RunQuery(VdcQueryType.GetJobByJobId,
                    parameters, _asyncQuery);
            return false;
        }
        return true;
    }

    public void removeSingleTask(Guid guid) {
        //detailTaskGuids.remove(guid);
    }

}
