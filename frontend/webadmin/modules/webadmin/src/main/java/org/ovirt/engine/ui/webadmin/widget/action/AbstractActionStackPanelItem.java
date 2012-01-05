package org.ovirt.engine.ui.webadmin.widget.action;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractActionStackPanelItem<M, T, W extends Widget> extends Composite {

    @UiField(provided = true)
    public SimpleActionPanel<T> actionPanel;

    @UiField(provided = true)
    public W dataDisplayWidget;

    public AbstractActionStackPanelItem(M modelProvider) {
        this.dataDisplayWidget = createDataDisplayWidget(modelProvider);
        this.actionPanel = createActionPanel(modelProvider);
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);

        // Add context menu handler for data display widget
        actionPanel.addContextMenuHandler(dataDisplayWidget);
    }

    public W getDataDisplayWidget() {
        return dataDisplayWidget;
    }

    protected abstract W createDataDisplayWidget(M modelProvider);

    protected abstract SimpleActionPanel<T> createActionPanel(M modelProvider);

}
