package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification.NotificationStatus;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TasksPresenterWidget extends AbstractOverlayPresenterWidget<TasksPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractOverlayPresenterWidget.ViewDef {
        void updateTaskStatus(TaskListModel taskListModel);
    }

    private final TaskModelProvider taskModelProvider;

    private Set<String> runningTasks = new HashSet<>();

    private static final List<ActionType> ACTION_TYPE_WHITELIST = Collections.unmodifiableList(Arrays.asList(
            ActionType.InitVdsOnUp
    ));

    private static Date lastNotificationDate = new Date();

    @Inject
    public TasksPresenterWidget(EventBus eventBus, ViewDef view, TaskModelProvider taskModelProvider) {
        super(eventBus, view);
        this.taskModelProvider = taskModelProvider;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().updateTaskStatus(taskModelProvider.getModel());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBind() {
        super.onBind();
        taskModelProvider.getModel().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                getView().updateTaskStatus(taskModelProvider.getModel());
                Collection<Job> jobs = taskModelProvider.getModel().getItems();
                jobs.forEach(job -> {
                    String id = job.getCorrelationId().startsWith(TaskListModel.WEBADMIN) ? job.getCorrelationId() : job.getId()
                                    .toString();
                    if (JobExecutionStatus.STARTED.equals(job.getStatus())) {
                        // Since its a set, it will be replaced when its a duplicate.
                        runningTasks.add(id);
                    } else if (JobExecutionStatus.FINISHED.equals(job.getStatus())
                            || JobExecutionStatus.FAILED.equals(job.getStatus())
                            || JobExecutionStatus.ABORTED.equals(job.getStatus())) {
                        if (runningTasks.contains(id) ||
                                (ACTION_TYPE_WHITELIST.contains(job.getActionType()) &&
                                        job.getEndTime().after(lastNotificationDate))) {
                            ToastNotification notification = ToastNotification.createNotification(
                                    getPrefixText(job.getStatus()) + " " + job.getDescription());//$NON-NLS-1$
                            notification.setStatus(getNotificationStatus(job.getStatus()));
                        }
                        runningTasks.remove(id);
                    }
                    lastNotificationDate = new Date();
                });
            }

        });
    }

    public static String getPrefixText(JobExecutionStatus status) {
        return EnumTranslator.getInstance().translate(status);
    }

    protected NotificationStatus getNotificationStatus(JobExecutionStatus status) {
        switch (status) {
        case ABORTED:
            return NotificationStatus.WARNING;
        case FAILED:
            return NotificationStatus.DANGER;
        case FINISHED:
            return NotificationStatus.SUCCESS;
        case STARTED:
            return NotificationStatus.INFO;
        case UNKNOWN:
            return NotificationStatus.WARNING;
        default:
            return NotificationStatus.INFO;
        }
    }
}
