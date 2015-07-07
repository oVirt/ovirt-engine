package org.ovirt.engine.ui.userportal.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class VerticalTabPanel extends AbstractTabPanel {

    interface WidgetUiBinder extends UiBinder<Widget, VerticalTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel tabContainer;

    public VerticalTabPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void addTabWidget(IsWidget tabWidget, int index) {
        tabContainer.insert(tabWidget, index);
    }

    @Override
    public void removeTabWidget(IsWidget tabWidget) {
        tabContainer.getElement().removeChild(tabWidget.asWidget().getElement());
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return new VerticalTab(tabData, this);
    }

}
