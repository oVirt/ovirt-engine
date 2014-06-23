package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.List;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TaskModelProvider extends SearchableTabModelProvider<Job, TaskListModel> {

    public interface TaskHandler {

        void onTaskCountChange(int count);

        void onRunningTasksCountChange(int count);

        void updateTree();

    }

    private TaskHandler taskHandler;
    private int lastRunningTasksCount = 0;

    @Inject
    public TaskModelProvider(EventBus eventBus,
            Provider<DefaultConfirmationPopupPresenterWidget> defaultConfirmPopupProvider) {
        super(eventBus, defaultConfirmPopupProvider);
    }

    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    protected void updateDataProvider(List<Job> items) {
        if (taskHandler != null) {
            taskHandler.onTaskCountChange(items.size());
        }
        int count = 0;
        for (Job job : items) {
            if (job.getStatus().equals(JobExecutionStatus.STARTED)) {
                ++count;
            }
        }
        if (count != lastRunningTasksCount) {
            lastRunningTasksCount = count;
            if (taskHandler != null) {
                taskHandler.onRunningTasksCountChange(count);
            }
        }

        super.updateDataProvider(items);
    }

    @Override
    protected void initializeModelHandlers(TaskListModel model) {
        super.initializeModelHandlers(model);
        if (taskHandler != null) {
            taskHandler.updateTree();
        }
    }
}
