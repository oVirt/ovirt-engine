package org.ovirt.engine.ui.webadmin.widget;

import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.ActionButton;
import org.ovirt.engine.ui.webadmin.widget.table.ActionButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel.AbstractSelectionModel;

public class SimpleActionPanel<T> extends AbstractActionPanel<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionPanel<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    ButtonBase refreshButton;

    public SimpleActionPanel(SearchableModelProvider<T, ?> dataProvider, AbstractSelectionModel<T> selectionModel) {
        super(dataProvider, selectionModel);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        return new SimpleActionButton();
    }

    @UiHandler("refreshButton")
    void handleRefreshPageButtonClick(ClickEvent event) {
        dataProvider.getModel().getForceRefreshCommand().Execute();
    }
}
