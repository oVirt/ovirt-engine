package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.queries.GetJobByJobIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetJobsByCorrelationIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetJobsByOffsetQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class TaskListModel extends SearchableListModel {

    public static final String _WEBADMIN_ = "_WEBADMIN_";
    private final Map<String, Job> detailedTaskMap = new HashMap<String, Job>();

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
                if (taskList.size() == 0) {
                    detailedTaskMap.clear();
                }
                ArrayList<Job> taskListWithCorrelationFilter = new ArrayList<Job>();

                Map<String, Map.Entry<Job, ArrayList<Job>>> correlationTaskMap =
                        new HashMap<String, Map.Entry<Job, ArrayList<Job>>>();
                for (Job task : taskList) {
                    if (task.getCorrelationId().startsWith(_WEBADMIN_)) {
                        if (!correlationTaskMap.containsKey(task.getCorrelationId())) {
                            Entry<Job, ArrayList<Job>> entry = new TaskEntry(task);
                            entry.setValue(new ArrayList<Job>());
                            correlationTaskMap.put(task.getCorrelationId(), entry);
                            String[] taskDeskArray = task.getCorrelationId().replace(_WEBADMIN_, "").split("_");
                            String taskDesk = "";
                            for (int i = 1; i < taskDeskArray.length; i++) {
                                taskDesk += taskDeskArray[i] + " ";
                            }
                            task.setId(Guid.NewGuid());
                            task.setDescription(taskDesk);
                            taskListWithCorrelationFilter.add(task);
                        }
                        Entry<Job, ArrayList<Job>> entry = correlationTaskMap.get(task.getCorrelationId());
                        entry.getValue().add(task);
                    } else {
                        taskListWithCorrelationFilter.add(task);
                    }
                }

                for (Entry<Job, ArrayList<Job>> entry : correlationTaskMap.values()) {
                    entry.getKey().setStatus(JobExecutionStatus.FINISHED);
                    for (Job task : entry.getValue()) {
                        if (!task.getStatus().equals(JobExecutionStatus.FINISHED)
                                && !entry.getKey().getStatus().equals(JobExecutionStatus.FINISHED)) {
                            entry.getKey().setStatus(task.getStatus());
                        }
                        if (entry.getKey().getLastUpdateTime() == null
                                || (entry.getKey().getLastUpdateTime().before(task.getLastUpdateTime())
                                && !entry.getKey().getLastUpdateTime().equals(task.getLastUpdateTime()))) {
                            entry.getKey().setLastUpdateTime(task.getLastUpdateTime());
                        }
                        Date tempDate = task.getLastUpdateTime();
                        if (entry.getKey().getStartTime() == null
                                || entry.getKey().getStartTime().after(task.getStartTime())) {
                            entry.getKey().setStartTime(task.getStartTime());
                        }

                        if (entry.getKey().getEndTime() == null
                                || entry.getKey().getEndTime().before(task.getEndTime())) {
                            entry.getKey().setEndTime(task.getEndTime());
                        }
                        entry.getKey().setLastUpdateTime(tempDate);
                    }
                }

                ArrayList<Job> newTaskList = new ArrayList<Job>();

                for (Job task : taskListWithCorrelationFilter) {
                    String id = "";
                    if (task.getCorrelationId().startsWith(_WEBADMIN_)) {
                        id = task.getCorrelationId();
                    } else {
                        id = task.getId().toString();
                    }
                    boolean hadDetails = detailedTaskMap.containsKey(id);
                    if (hadDetails
                            && task.getLastUpdateTime().getTime()
                                    - detailedTaskMap.get(id).getLastUpdateTime().getTime() < 100) {
                        task.setSteps(detailedTaskMap.get(id).getSteps());
                    } else if (hadDetails) {
                        detailedTaskMap.remove(id.toString());
                        updateSingleTask(id);
                    }

                    newTaskList.add(task);
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

    public boolean updateSingleTask(final String guidOrCorrelationId) {
        if (!detailedTaskMap.containsKey(guidOrCorrelationId)) {
            detailedTaskMap.put(guidOrCorrelationId.toString(), null);
            AsyncQuery _asyncQuery = new AsyncQuery();
            _asyncQuery.setModel(this);

            if (guidOrCorrelationId.startsWith(_WEBADMIN_)) {
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object ReturnValue)
                    {
                        TaskListModel taskListModel = (TaskListModel) model;
                        ArrayList<Job> retTasks = (ArrayList<Job>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();

                        ArrayList<Job> taskList = (ArrayList<Job>) taskListModel.getItems();
                        ArrayList<Job> newTaskList = new ArrayList<Job>();
                        for (Job task : taskList) {
                            if (task.getCorrelationId().equals(guidOrCorrelationId)) {
                                detailedTaskMap.put(guidOrCorrelationId, task);
                                task.setStatus(JobExecutionStatus.FINISHED);
                                for (Job job : retTasks) {
                                    Step step = new Step();
                                    step.setId(job.getId());
                                    step.setDescription(job.getDescription());
                                    step.setCorrelationId(job.getCorrelationId());
                                    step.setStartTime(job.getStartTime());
                                    step.setEndTime(job.getEndTime());
                                    step.setStatus(job.getStatus());
                                    step.setSteps(job.getSteps());
                                    if (!task.getStatus().equals(JobExecutionStatus.FINISHED)
                                            && !job.getStatus().equals(JobExecutionStatus.FINISHED)) {
                                        task.setStatus(job.getStatus());
                                    }
                                    if (task.getLastUpdateTime() == null
                                            || (task.getLastUpdateTime().before(job.getLastUpdateTime()) && !task.getLastUpdateTime()
                                                    .equals(job.getLastUpdateTime()))) {
                                        task.setLastUpdateTime(job.getEndTime());
                                    }
                                    Date tempDate = task.getLastUpdateTime();
                                    if (task.getStartTime() == null
                                            || task.getStartTime().after(job.getStartTime())) {
                                        task.setStartTime(job.getStartTime());
                                    }

                                    if (task.getEndTime() == null
                                            || task.getEndTime().before(job.getEndTime())) {
                                        task.setEndTime(job.getEndTime());
                                    }
                                    task.addStep(step);
                                    task.setLastUpdateTime(tempDate);

                                }

                            }
                            newTaskList.add(task);
                        }

                        taskListModel.setItems(newTaskList);
                    }
                };
                GetJobsByCorrelationIdQueryParameters parameters = new GetJobsByCorrelationIdQueryParameters();
                parameters.setCorrelationId(guidOrCorrelationId);
                Frontend.RunQuery(VdcQueryType.GetJobsByCorrelationId,
                        parameters, _asyncQuery);
            } else {
                _asyncQuery.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object ReturnValue)
                    {
                        TaskListModel taskListModel = (TaskListModel) model;
                        Job retTask = (Job) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        detailedTaskMap.put(retTask.getId().toString(), retTask);
                        ArrayList<Job> taskList = (ArrayList<Job>) taskListModel.getItems();
                        ArrayList<Job> newTaskList = new ArrayList<Job>();
                        for (Job task : taskList) {
                            if (task.getId().equals(retTask.getId())) {
                                newTaskList.add(retTask);
                            } else {
                                newTaskList.add(task);
                            }
                        }

                        taskListModel.setItems(newTaskList);
                    }
                };
                GetJobByJobIdQueryParameters parameters = new GetJobByJobIdQueryParameters();
                parameters.setJobId(new Guid(guidOrCorrelationId));
                Frontend.RunQuery(VdcQueryType.GetJobByJobId,
                        parameters, _asyncQuery);
            }
            return false;
        }
        return true;
    }

    public void removeSingleTask(String string) {
        //detailTaskGuids.remove(guid);
    }

    class TaskEntry implements Map.Entry<Job, ArrayList<Job>> {
        public TaskEntry(Job key) {
            this.key = key;
        }

        Job key;
        ArrayList<Job> list;

        @Override
        public Job getKey() {
            return key;
        }

        @Override
        public ArrayList<Job> getValue() {
            return list;
        }

        @Override
        public ArrayList<Job> setValue(ArrayList<Job> value) {
            list = value;
            return list;
        }

    }

}
