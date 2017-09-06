package org.ovirt.engine.ui.webadmin.section.main.view.overlay;

import java.util.Collection;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.uicommon.tasks.TaskWidget;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TasksPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

public class TasksView extends AbstractView implements TasksPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<Container, TasksView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    FormGroup tasksGroup;

    @UiField
    Column emptyTasksColumn;

    @UiField
    Button closeButton;

    public TasksView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateTaskStatus(TaskListModel tasksListModel) {
        tasksGroup.clear();
        Collection<Job> jobs = tasksListModel.getItems();
        if (jobs != null) {
            jobs.forEach(job -> {
                String id = job.getCorrelationId().startsWith(TaskListModel.WEBADMIN) ? job.getCorrelationId() : job.getId()
                                .toString();
                tasksGroup.add(new TaskWidget(job, id, tasksListModel));
            });
            emptyTasksColumn.setVisible(jobs.isEmpty());
        } else {
            emptyTasksColumn.setVisible(true);
        }
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }
}
