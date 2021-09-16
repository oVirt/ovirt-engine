package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification.NotificationStatus;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.NotificationPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class TasksPresenterWidget extends AbstractOverlayPresenterWidget<TasksPresenterWidget.ViewDef> {

    protected final Logger log = Logger.getLogger(TasksPresenterWidget.class.getName());

    public interface ViewDef extends AbstractOverlayPresenterWidget.ViewDef {
        void updateTaskStatus(TaskListModel taskListModel);
    }

    private final TaskModelProvider taskModelProvider;

    private final NotificationPresenterWidget notificationPresenterWidget;

    private final Set<String> runningTasks = new HashSet<>();

    /**
     * "special" tasks have(or require) no "start" event, so they may be missing in the {@link #runningTasks}. They are
     * displayed if their end time is newer the last displayed "special" task (check {@link #isNewSpecialTask}).
     */
    private static final List<ActionType> ACTION_TYPE_WHITELIST = Collections.singletonList(
            /*
             * This event is not (directly) triggered by the user from the UI (no START event or shared correlation ID).
             * Main use case: confirmation that the host is up after activating the host from maintenance mode.
             */
            ActionType.InitVdsOnUp);

    private static final long NOT_INITIALIZED_END_TIME = 0L;
    private static final long DEFAULT_END_TIME = 1L;
    private long lastSpecialTaskEndTime = NOT_INITIALIZED_END_TIME;

    @Inject
    public TasksPresenterWidget(EventBus eventBus,
            ViewDef view,
            TaskModelProvider taskModelProvider,
            NotificationPresenterWidget notificationPresenterWidget) {
        super(eventBus, view);
        this.taskModelProvider = taskModelProvider;
        this.notificationPresenterWidget = notificationPresenterWidget;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().updateTaskStatus(taskModelProvider.getModel());
    }

    @Override
    public void onBind() {
        super.onBind();
        taskModelProvider.getModel().getItemsChangedEvent().addListener((ev, sender, args) -> {
            getView().updateTaskStatus(taskModelProvider.getModel());
            if (lastSpecialTaskEndTime == NOT_INITIALIZED_END_TIME) {
                // all tasks retrieved on initial load are considered "old"
                // listener might get fired before the data is loaded
                // don't repeat initialization only if jobs were successfully fetched
                lastSpecialTaskEndTime = findLatestSpecialTaskEndTime(
                        taskModelProvider.getModel().getItemsFromFirstLoad(),
                        DEFAULT_END_TIME);
            }
            Collection<Job> jobs = taskModelProvider.getModel().getItems();
            jobs.forEach(job -> {
                String id = job.getCorrelationId().startsWith(TaskListModel.WEBADMIN) ? job.getCorrelationId()
                        : job.getId().toString();
                if (JobExecutionStatus.STARTED.equals(job.getStatus())) {
                    // Since it's a set, it will be replaced when it's a duplicate.
                    runningTasks.add(id);
                } else if (JobExecutionStatus.FINISHED.equals(job.getStatus())
                        || JobExecutionStatus.FAILED.equals(job.getStatus())
                        || JobExecutionStatus.ABORTED.equals(job.getStatus())) {

                    if (runningTasks.contains(id) || isNewSpecialTask(job)) {
                        log.fine("Create notification for job: " + job); //$NON-NLS-1$
                        notificationPresenterWidget.createNotification(
                                getPrefixText(job.getStatus()) + " " + job.getDescription(), //$NON-NLS-1$
                                getNotificationStatus(job.getStatus()));
                    }
                    runningTasks.remove(id);
                }
            });

            lastSpecialTaskEndTime = findLatestSpecialTaskEndTime(jobs, lastSpecialTaskEndTime);
        });
    }

    private long findLatestSpecialTaskEndTime(Collection<Job> jobs, long previousLastSpecialTaskEndTime) {
        return Optional.ofNullable(jobs)
                .map(Collection::stream)
                .map(stream -> stream
                        .filter(this::isSpecialTask)
                        .map(Job::getEndTime)
                        .filter(Objects::nonNull)
                        .map(Date::getTime)
                        .reduce(previousLastSpecialTaskEndTime, Math::max))
                .orElse(NOT_INITIALIZED_END_TIME);
    }

    private boolean isSpecialTask(Job job) {
        return ACTION_TYPE_WHITELIST.contains(job.getActionType());
    }

    private boolean isNewSpecialTask(Job job) {
        // assumptions:
        // end time is assigned in one place for each "special" job (no time sync issues)
        // there are no out-of-order notifications (new items always have strictly higher end time)
        return isSpecialTask(job) &&
                job.getEndTime() != null &&
                job.getEndTime().getTime() > lastSpecialTaskEndTime;
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
