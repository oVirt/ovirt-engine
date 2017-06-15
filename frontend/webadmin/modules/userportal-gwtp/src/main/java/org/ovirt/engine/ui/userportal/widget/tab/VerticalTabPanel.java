package org.ovirt.engine.ui.userportal.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
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
    protected TabDefinition createNewTab(TabData tabData) {
        return new VerticalTab(tabData, this);
    }

    @Override
    public void addTabDefinition(Tab tab, int index) {
        tabContainer.insert(tab.asWidget(), index);
    }

    @Override
    public void removeTabDefinition(Tab tab) {
        tabContainer.getElement().removeChild(tab.asWidget().getElement());
    }

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        // TODO Auto-generated method stub
        return null;
    }

}
