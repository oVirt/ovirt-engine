package org.ovirt.engine.ui.webadmin.widget.dialog.tab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DialogTabPanel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, DialogTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel tabContainer;

    @UiField
    SimplePanel tabContentContainer;

    private DialogTab activeTab;

    @UiConstructor
    public DialogTabPanel(String height) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        getWidget().setHeight(height);
    }

    @UiChild(tagname = "tab")
    public void addTab(final DialogTab tab) {
        tabContainer.add(tab);

        tab.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                switchTab(tab);
            }
        });

        // Switch to first tab automatically
        if (tabContainer.getWidgetCount() == 1) {
            switchTab(tab);
        }
    }

    public void switchTab(DialogTab tab) {
        setActiveTab(tab);
        setTabContent(tab.getContent());
    }

    void setActiveTab(DialogTab tab) {
        if (activeTab != null)
            activeTab.deactivate();

        if (tab != null)
            tab.activate();

        activeTab = tab;
    }

    void setTabContent(Widget content) {
        tabContentContainer.setWidget(content);
    }

}
