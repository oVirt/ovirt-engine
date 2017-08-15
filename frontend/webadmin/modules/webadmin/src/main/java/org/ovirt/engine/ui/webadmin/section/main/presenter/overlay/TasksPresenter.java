package org.ovirt.engine.ui.webadmin.section.main.presenter.overlay;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.ToastNotification.NotificationStatus;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractOverlayPresenter;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Proxy;

public class TasksPresenter extends AbstractOverlayPresenter<TasksPresenter.ViewDef, TasksPresenter.ProxyDef> {

    public interface ViewDef extends AbstractOverlayPresenter.ViewDef {
        void updateTaskStatus(TaskListModel taskListModel);
    }

    @ProxyCodeSplit
    public interface ProxyDef extends Proxy<TasksPresenter> {
    }

    private final TaskModelProvider taskModelProvider;

    private Set<String> runningTasks = new HashSet<>();

    @Inject
    public TasksPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy, TaskModelProvider taskModelProvider) {
        super(eventBus, view, proxy);
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
                        if (runningTasks.contains(id)) {
                            ToastNotification notification = ToastNotification.createNotification(
                                    getPrefixText(job.getStatus()) + " " + job.getDescription());//$NON-NLS-1$
                            notification.setStatus(getNotificationStatus(job.getStatus()));
                        }
                        runningTasks.remove(id);
                    }
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
