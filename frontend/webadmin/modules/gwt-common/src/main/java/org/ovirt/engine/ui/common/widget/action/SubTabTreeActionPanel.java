package org.ovirt.engine.ui.common.widget.action;

import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.SearchableModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class SubTabTreeActionPanel<T> extends AbstractActionPanel<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SubTabTreeActionPanel<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public SubTabTreeActionPanel(SearchableModelProvider<T, ?> dataProvider, EventBus eventBus) {
        super(dataProvider, eventBus);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public List<T> getSelectedItems() {
        return getDataProvider().getModel().getSelectedItems();
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        return new SimpleActionButton();
    }

}
