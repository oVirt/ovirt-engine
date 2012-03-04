package org.ovirt.engine.ui.webadmin.widget.footer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.webadmin.widget.table.column.TaskStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.tree.AbstractTaskSubTabTree;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;

public class TasksTree extends AbstractTaskSubTabTree<TaskListModel> {
    ArrayList<String> openTasks = new ArrayList<String>();
    ArrayList<String> openSteps = new ArrayList<String>();

    @Override
    public void updateTree(final TaskListModel listModel) {
        tree.addOpenHandler(new OpenHandler<TreeItem>() {

            @Override
            public void onOpen(OpenEvent<TreeItem> event) {
                TreeItem item = event.getTarget();
                String guidOrCorrelationId = "";
                if (item.getParentItem() != null) {
                    guidOrCorrelationId = item.getElement().getId();
                    if (!openSteps.contains(guidOrCorrelationId)) {
                        openSteps.add(guidOrCorrelationId);
                    }
                    return;
                }
                guidOrCorrelationId = item.getElement().getId();
                if (item.getChildCount() == 1) {
                    item.addItem(new Label("Loading..."));
                }
                openTasks.add(guidOrCorrelationId);
                listModel.updateSingleTask(guidOrCorrelationId);
            }
        });

        tree.addCloseHandler(new CloseHandler<TreeItem>() {

            @Override
            public void onClose(CloseEvent<TreeItem> event) {
                String guidOrCorrelationId = null;
                TreeItem item = event.getTarget();
                if (item.getParentItem() != null) {
                    guidOrCorrelationId = item.getElement().getId();
                    openSteps.remove(guidOrCorrelationId);
                    return;
                }
                guidOrCorrelationId = item.getElement().getId();
                openTasks.remove(guidOrCorrelationId);
                listModel.removeSingleTask(item.getElement().getId());
            }
        });

        listModel.getItemsChangedEvent().addListener(new IEventListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                TaskListModel model = (TaskListModel) sender;
                final List<Job> tasks = (List<Job>) model.getItems();

                tree.clear();
                if (tasks == null || tasks.size() == 0) {
                    openTasks.clear();
                    openSteps.clear();
                    return;
                }
                ArrayList<String> currentTasks = new ArrayList<String>();

                for (Job task : tasks) {
                    String id = "";
                    if (task.getCorrelationId().startsWith(TaskListModel._WEBADMIN_)) {
                        id = task.getCorrelationId();
                    } else {
                        id = task.getId().toString();
                    }
                    currentTasks.add(id);

                    TreeItem taskItem = getJobNode(task);

                    taskItem.getElement().setId(id);
                    boolean isTaskOpen = openTasks.contains(id);
                    if (task.getSteps() == null || task.getSteps().size() == 0) {
                        taskItem.addItem(new TreeItem());
                    } else {
                        addItem(task.getSteps(), taskItem);
                    }
                    taskItem.setState(isTaskOpen);
                    openSteps(taskItem);

                    tree.addItem(taskItem);
                }
                ArrayList<String> removedTasks = new ArrayList<String>();
                for (String guidOrCorrelationId : openTasks) {
                    if (!currentTasks.contains(guidOrCorrelationId)) {
                        removedTasks.add(guidOrCorrelationId);
                    }
                }
                openTasks.removeAll(removedTasks);
            }

            private void openSteps(TreeItem taskItem) {
                if (taskItem == null || taskItem.getElement().getId() == null) {
                    return;
                }
                for (int i = 0; i < taskItem.getChildCount(); i++) {
                    String stepId = taskItem.getChild(i).getElement().getId();
                    boolean isOpen = openSteps.contains(stepId);
                    taskItem.getChild(i).setState(isOpen);
                    openSteps(taskItem.getChild(i));
                }
            }

            private void addItem(List<Step> list, TreeItem treeItem) {
                if (list == null || list.size() == 0) {
                    return;
                }
                TreeItem stepItem = null;
                for (Step step : list) {
                    stepItem = getStepNode(step);
                    Guid guid = step.getId();
                    stepItem.getElement().setId(guid.toString());
                    treeItem.addItem(stepItem);
                    addItem(step.getSteps(), stepItem);
                }
            }
        });
    }

    private TreeItem getJobNode(Job task) {
        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        (Resources) GWT.create(TaskTreeHeaderlessTableResources.class),
                        true);

        table.addColumn(new TaskStatusColumn(), "Status", "30px");

        FullDateTimeColumn<EntityModel> timeColumn = new FullDateTimeColumn<EntityModel>() {
            @Override
            protected Date getRawValue(EntityModel entity) {
                Job object = (Job) entity.getEntity();
                return object.getEndTime() == null ? object.getStartTime() : object.getEndTime();
            }
        };
        table.addColumn(timeColumn, "Time", "160px");

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                Job object = (Job) entity.getEntity();
                return object.getDescription();
            }
        };
        table.addColumn(descriptionColumn, "Description");

        ArrayList<EntityModel> entityModelList = toEntityModelList(new ArrayList<Job>(Arrays.asList(task)));
        return createTreeItem(table, entityModelList);
    }

    private TreeItem getStepNode(Step step) {
        EntityModelCellTable<ListModel> table =
                new EntityModelCellTable<ListModel>(false,
                        (Resources) GWT.create(TaskTreeHeaderlessTableResources.class),
                        true);

        table.addColumn(new TaskStatusColumn(), "Status", "30px");

        FullDateTimeColumn<EntityModel> timeColumn = new FullDateTimeColumn<EntityModel>() {
            @Override
            protected Date getRawValue(EntityModel entity) {
                Step object = (Step) entity.getEntity();
                return object.getEndTime() == null ? object.getStartTime() : object.getEndTime();
            }
        };
        table.addColumn(timeColumn, "Time", "160px");

        TextColumnWithTooltip<EntityModel> descriptionColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel entity) {
                Step object = (Step) entity.getEntity();
                return object.getDescription();
            }
        };
        table.addColumn(descriptionColumn, "Description");

        ArrayList<EntityModel> entityModelList = toEntityModelList(new ArrayList<Step>(Arrays.asList(step)));
        return createTreeItem(table, entityModelList);
    }

    public interface TaskTreeHeaderlessTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TaskTreeHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    public void collapseAllTasks() {
        openTasks.clear();
        openSteps.clear();
        for (int i = 0; i < tree.getItemCount(); i++) {
            collapseAllTasksHelper(tree.getItem(i));
        }
    }

    private void collapseAllTasksHelper(TreeItem item) {
        item.setState(false);
        for (int i = 0; i < item.getChildCount(); i++) {
            collapseAllTasksHelper(item.getChild(i));
        }
    }
}
