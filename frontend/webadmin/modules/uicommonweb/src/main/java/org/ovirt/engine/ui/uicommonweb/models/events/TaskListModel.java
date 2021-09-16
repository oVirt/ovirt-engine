package org.ovirt.engine.ui.uicommonweb.models.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.queries.GetJobsByCorrelationIdQueryParameters;
import org.ovirt.engine.core.common.queries.GetJobsByOffsetQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class TaskListModel extends SearchableListModel<Void, Job> {

    public static final String WEBADMIN = "_WEBADMIN_"; //$NON-NLS-1$
    private final Map<String, Job> detailedTaskMap = new HashMap<>();
    // null means that syncSearch was not run yet
    private List<Job> itemsFromFirstLoad = null;

    public TaskListModel() {
        getSearchCommand().execute();
    }

    public List<Job> getItemsFromFirstLoad() {
        return itemsFromFirstLoad;
    }

    @Override
    protected void syncSearch() {
        AsyncQuery<QueryReturnValue> asyncQuery = new AsyncQuery<>(returnValue -> {
            List<Job> taskList = returnValue.getReturnValue();
            if (taskList.size() == 0) {
                detailedTaskMap.clear();
            }
            List<Job> taskListWithCorrelationFilter = new ArrayList<>();

            Map<String, Map.Entry<Job, ArrayList<Job>>> correlationTaskMap = new HashMap<>();
            for (Job task : taskList) {
                if (task.getCorrelationId().startsWith(WEBADMIN)) {
                    if (!correlationTaskMap.containsKey(task.getCorrelationId())) {
                        Job rootTask = new Job();
                        rootTask.setCorrelationId(task.getCorrelationId());
                        Map.Entry<Job, ArrayList<Job>> entry = new TaskEntry(rootTask);
                        entry.setValue(new ArrayList<Job>());
                        correlationTaskMap.put(rootTask.getCorrelationId(), entry);
                        String[] taskDescreptionArray =
                                rootTask.getCorrelationId().replace(WEBADMIN, "").split("_"); //$NON-NLS-1$ //$NON-NLS-2$
                        StringBuilder taskDesc = new StringBuilder();
                        for (int i = 1; i < taskDescreptionArray.length; i++) {
                            taskDesc.append(taskDescreptionArray[i]).append(" "); //$NON-NLS-1$
                        }
                        rootTask.setId(task.getId());
                        rootTask.setDescription(taskDesc.toString());
                        taskListWithCorrelationFilter.add(rootTask);
                    }
                    Map.Entry<Job, ArrayList<Job>> entry = correlationTaskMap.get(task.getCorrelationId());
                    entry.getValue().add(task);
                } else {
                    taskListWithCorrelationFilter.add(task);
                }
            }

            for (Map.Entry<Job, ArrayList<Job>> entry : correlationTaskMap.values()) {
                entry.getKey().setStatus(JobExecutionStatus.UNKNOWN);
                boolean hasFailedStatus = false;
                boolean hasStartedStatus = false;
                boolean hasAbortedStatus = false;
                int finishedCount = 0;

                for (Job task : entry.getValue()) {
                    switch (task.getStatus()) {
                        case STARTED:
                            hasStartedStatus = true;
                            break;
                        case FINISHED:
                            finishedCount++;
                            break;
                        case FAILED:
                            hasFailedStatus = true;
                            break;
                        case ABORTED:
                            hasAbortedStatus = true;
                            break;
                        case UNKNOWN:
                            break;
                        default:
                            break;
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
                if (hasFailedStatus) {
                    entry.getKey().setStatus(JobExecutionStatus.FAILED);
                } else if (finishedCount == entry.getValue().size()) {
                    entry.getKey().setStatus(JobExecutionStatus.FINISHED);
                } else if (hasStartedStatus) {
                    entry.getKey().setStatus(JobExecutionStatus.STARTED);
                } else if (hasAbortedStatus) {
                    entry.getKey().setStatus(JobExecutionStatus.ABORTED);
                } else {
                    entry.getKey().setStatus(JobExecutionStatus.UNKNOWN);
                }
            }

            ArrayList<Job> newTaskList = new ArrayList<>();

            for (Job task : taskListWithCorrelationFilter) {
                String id;
                if (task.getCorrelationId().startsWith(WEBADMIN)) {
                    id = task.getCorrelationId();
                } else {
                    id = task.getId().toString();
                }
                boolean hadDetails = detailedTaskMap.containsKey(id) && detailedTaskMap.get(id) != null;
                if (hadDetails
                        && task.getLastUpdateTime().getTime()
                        - detailedTaskMap.get(id).getLastUpdateTime().getTime() < 100) {
                    task.setSteps(detailedTaskMap.get(id).getSteps());
                } else if (hadDetails) {
                    detailedTaskMap.remove(id);
                    updateSingleTask(id);
                }

                newTaskList.add(task);
            }

            if (itemsFromFirstLoad == null) {
                itemsFromFirstLoad = newTaskList;
            }
            setItems(newTaskList);
        });
        GetJobsByOffsetQueryParameters tempVar = new GetJobsByOffsetQueryParameters();
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetJobsByOffset,
                tempVar, asyncQuery);

        setIsQueryFirstTime(false);
    }

    @Override
    protected String getListName() {
        return "TaskListModel"; //$NON-NLS-1$
    }

    public boolean updateSingleTask(final String guidOrCorrelationId) {
        if (!detailedTaskMap.containsKey(guidOrCorrelationId)) {
            detailedTaskMap.put(guidOrCorrelationId, null);
            if (guidOrCorrelationId.startsWith(WEBADMIN)) {
                GetJobsByCorrelationIdQueryParameters parameters = new GetJobsByCorrelationIdQueryParameters();
                parameters.setCorrelationId(guidOrCorrelationId);
                Frontend.getInstance().runQuery(QueryType.GetJobsByCorrelationId,
                        parameters, new AsyncQuery<QueryReturnValue>(returnValue -> {
                            ArrayList<Job> retTasks = returnValue.getReturnValue();

                            ArrayList<Job> taskList = (ArrayList<Job>) getItems();
                            ArrayList<Job> newTaskList = new ArrayList<>();
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

                                        if (task.getEndTime() != null && (task.getEndTime() == null
                                                || task.getEndTime().before(job.getEndTime()))) {
                                            task.setEndTime(job.getEndTime());
                                        }
                                        task.addStep(step);
                                        task.setLastUpdateTime(tempDate);

                                    }

                                }
                                newTaskList.add(task);
                            }

                            setItems(newTaskList);
                        }));
            } else {
                IdQueryParameters parameters = new IdQueryParameters(new Guid(guidOrCorrelationId));
                Frontend.getInstance().runQuery(QueryType.GetJobByJobId,
                        parameters, new AsyncQuery<QueryReturnValue>(returnValue -> {
                            Job retTask = returnValue.getReturnValue();
                            if (retTask == null) {
                                return;
                            }
                            detailedTaskMap.put(retTask.getId().toString(), retTask);
                            ArrayList<Job> taskList = (ArrayList<Job>) getItems();
                            ArrayList<Job> newTaskList = new ArrayList<>();
                            for (Job task : taskList) {
                                if (task.getId().equals(retTask.getId())) {
                                    newTaskList.add(retTask);
                                } else {
                                    newTaskList.add(task);
                                }
                            }

                            setItems(newTaskList);
                        }));
            }
            return false;
        }
        return true;
    }

    static class TaskEntry implements Map.Entry<Job, ArrayList<Job>> {
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

    /**
     * @param actionDescription the name of the action (e.g. "Remove multiple disk from vm-Win7") cannot exceed 40 chars.
     * @return frontend correlation id Description
     * example: _WEBADMIN_098437232_Remove_multiple_disk_from_vm-Win7
     */
    public static String createCorrelationId(String actionDescription) {
        actionDescription = actionDescription.replace(' ', '_');
        Random rand = new Random();
        String hashStr = rand.nextInt(9000000) + 9999999 + ""; //$NON-NLS-1$

        return TaskListModel.WEBADMIN + hashStr + "_" + actionDescription; //$NON-NLS-1$
    }
}
