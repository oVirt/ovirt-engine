package org.ovirt.engine.ui.common.widget.action;

import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Widget;

public class SimpleActionPanel<T> extends AbstractActionPanel<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionPanel<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    ButtonBase refreshButton;

    public SimpleActionPanel(SearchableModelProvider<T, ?> dataProvider) {
        super(dataProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @UiHandler("refreshButton")
    void handleRefreshButtonClick(ClickEvent event) {
        getDataProvider().getModel().getForceRefreshCommand().execute();
    }

    @Override
    public List<T> getSelectedItems() {
        if (getDataProvider().getModel().isSingleSelectionOnly()) {
            return (List<T>) Collections.singletonList(getDataProvider().getModel().getSingleSelectionModel().getSelectedObject());
        } else {
            return getDataProvider().getModel().getOrderedMultiSelectionModel().getSelectedList();
        }
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        SimpleActionButton result = new SimpleActionButton();
        if (buttonDef.getIcon() instanceof IconType) {
            result.setIcon((IconType) buttonDef.getIcon());
        }
        return result;
    }

}
