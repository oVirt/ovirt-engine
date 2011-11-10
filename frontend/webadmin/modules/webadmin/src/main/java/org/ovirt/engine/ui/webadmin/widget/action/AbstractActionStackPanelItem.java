package org.ovirt.engine.ui.webadmin.widget.action;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractActionStackPanelItem<T> extends Composite {

    @UiField(provided = true)
    public SimpleActionPanel<T> actionPanel;

    @UiField(provided = true)
    public Widget dataDisplayWidget;

    public AbstractActionStackPanelItem(Widget dataDisplayWidget, SimpleActionPanel<T> actionPanel) {
        this.dataDisplayWidget = dataDisplayWidget;
        this.actionPanel = actionPanel;
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);

        // Add context menu handler for data display widget
        actionPanel.addContextMenuHandler(dataDisplayWidget);
    }

}
