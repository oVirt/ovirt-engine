package org.ovirt.engine.ui.webadmin.widget.footer;

import java.util.ArrayList;

import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.uicommonweb.models.events.TaskListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.label.FullDateTimeLabel;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class TasksTree extends AbstractSubTabTree<TaskListModel, Job, Step> {

    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public TasksTree() {
        super();
    }

    @Override
    protected TreeItem getRootItem(Job task) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); //$NON-NLS-1$

        addItemToPanel(panel, new Image(getStatusImage(task.getStatus())), "25px"); //$NON-NLS-1$
        StringValueLabel descriptionTextBoxLabel = new StringValueLabel();
        descriptionTextBoxLabel.getElement().getStyle().setPaddingRight(5, Unit.PX);
        descriptionTextBoxLabel.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
        addTextBoxToPanel(panel, descriptionTextBoxLabel, task.getDescription(), ""); //$NON-NLS-1$
        addValueLabelToPanel(panel, new FullDateTimeLabel(), task.getStartTime(), "150px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), task.getEndTime() == null ? "" : constants.untilEndTime(), "80px"); //$NON-NLS-1$ //$NON-NLS-2$
        addValueLabelToPanel(panel, new FullDateTimeLabel(), task.getEndTime(), "150px"); //$NON-NLS-1$

        WidgetTooltip corrIdTextBoxLabelTooltip = new WidgetTooltip(new StringValueLabel());
        corrIdTextBoxLabelTooltip.setText(constants.correltaionIdEvent());
        corrIdTextBoxLabelTooltip.reconfigure();

        addTextBoxToPanel(panel,
                corrIdTextBoxLabelTooltip,
                task.getCorrelationId() != null && task.getCorrelationId().startsWith(TaskListModel.WEBADMIN) ? task.getCorrelationId()
                        .split("_")[2] : task.getCorrelationId(), "100px"); //$NON-NLS-1$ //$NON-NLS-2$

        TreeItem treeItem = new TreeItem(panel);
        String id =
                task.getCorrelationId().startsWith(TaskListModel.WEBADMIN) ? task.getCorrelationId() : task.getId()
                        .toString();
        treeItem.setUserObject(id);
        return treeItem;
    }

    @Override
    protected TreeItem getEmptyRoot() {
        return new TreeItem(new Label(constants.loadingLabel()));
    }

    @Override
    protected void addLeaves(TreeItem nodeItem, Step step) {
        if (step.getSteps() == null) {
            return;
        }
        TreeItem innerItem;
        for (Step innerStep : step.getSteps()) {
            innerItem = getNodeItem(innerStep);
            if (innerItem != null) {
                styleItem(innerItem, true);
                nodeItem.addItem(innerItem);
                addLeaves(innerItem, innerStep);
            }
        }
    }

    @Override
    protected void onTreeItemOpen(TreeItem treeItem) {
        super.onTreeItemOpen(treeItem);
        // Root node
        if (treeItem.getParentItem() == null) {
            String idOrCorrelationId = (String) treeItem.getUserObject();
            listModel.updateSingleTask(idOrCorrelationId);
        }
    }

    @Override
    protected TreeItem getLeafItem(Step step) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(1);
        panel.setWidth("100%"); //$NON-NLS-1$

        addItemToPanel(panel, new Image(getStatusImage(step.getStatus())), "25px"); //$NON-NLS-1$
        StringValueLabel descriptionTextBoxLabel = new StringValueLabel();
        descriptionTextBoxLabel.getElement().getStyle().setPaddingRight(5, Unit.PX);
        descriptionTextBoxLabel.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);
        addTextBoxToPanel(panel, descriptionTextBoxLabel, step.getDescription(), ""); //$NON-NLS-1$
        addValueLabelToPanel(panel, new FullDateTimeLabel(), step.getStartTime(), "150px"); //$NON-NLS-1$
        addTextBoxToPanel(panel, new StringValueLabel(), step.getEndTime() == null ? "" : constants.until(), "80px"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        addValueLabelToPanel(panel, new FullDateTimeLabel(), step.getEndTime(), "150px"); //$NON-NLS-1$

        TreeItem treeItem = new TreeItem(panel);
        treeItem.setUserObject(step.getId());
        return treeItem;
    }

    @Override
    protected TreeItem getNodeItem(Step step) {
        return getLeafItem(step);
    }

    @Override
    protected ArrayList<Step> getNodeObjects(Job task) {
        return (ArrayList<Step>) task.getSteps();
    }

    public void collapseAllTasks() {
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

    public ImageResource getStatusImage(JobExecutionStatus jobExecutionStatus) {
        if (jobExecutionStatus == null) {
            return resources.questionMarkImage();
        }
        switch (jobExecutionStatus) {
        case STARTED:
            return resources.waitImage();
        case FINISHED:
            return resources.logNormalImage();
        case FAILED:
            return resources.logErrorImage();
        case ABORTED:
            return resources.logWarningImage();
        case UNKNOWN:
            return resources.questionMarkImage();
        default:
            return null;
        }
    }

    @Override
    protected void addItemToPanel(HorizontalPanel panel, Widget item, String width) {
        super.addItemToPanel(panel, item, width);
        item.getElement().getStyle().setColor("white"); //$NON-NLS-1$
        item.getElement().getStyle().setVerticalAlign(VerticalAlign.MIDDLE);
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    }

    @Override
    protected void styleItem(TreeItem item, boolean enabled) {
        super.styleItem(item, enabled);
        item.getElement().getStyle().setProperty("borderTop", "1px solid white"); //$NON-NLS-1$ //$NON-NLS-2$
        if (item.getParentItem() != null) {
            item.getElement().getStyle().setBackgroundColor("grey"); //$NON-NLS-1$
        }
    }
}
