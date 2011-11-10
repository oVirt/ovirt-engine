package org.ovirt.engine.ui.webadmin.widget.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;

public class SimpleActionPanel<T> extends AbstractActionPanel<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionPanel<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private final SingleSelectionModel<T> selectionModel;

    @UiField
    ButtonBase refreshButton;

    public SimpleActionPanel(SearchableModelProvider<T, ?> dataProvider, SingleSelectionModel<T> selectionModel) {
        super(dataProvider);
        this.selectionModel = selectionModel;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent event) {
        getDataProvider().getModel().getForceRefreshCommand().Execute();
    }

    @Override
    protected List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<T>();
        selectedItems.add(selectionModel.getSelectedObject());
        return selectedItems;
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        return new SimpleActionButton();
    }

}
