package org.ovirt.engine.ui.common.widget.dialog.tab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
    SimplePanel tabHeaderContainer;

    @UiField
    FlowPanel tabContainer;

    @UiField
    SimplePanel tabContentContainer;

    private DialogTab activeTab;

    private static final int SPACE_KEY_CODE = 32;

    @UiConstructor
    public DialogTabPanel(String height) {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        getWidget().setHeight(height);
    }

    @UiChild(tagname = "header", limit = 1)
    public void setHeader(Widget widget) {
        tabHeaderContainer.getElement().getStyle().setDisplay(Display.BLOCK);
        tabHeaderContainer.add(widget);
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

        tab.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (SPACE_KEY_CODE == event.getNativeKeyCode()) {
                    switchTab(tab);
                }
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

        if (tab != null) {
            tab.activate();
        }

        activeTab = tab;
    }

    void setTabContent(Widget content) {
        tabContentContainer.setWidget(content);
    }

    public void addBarStyle(String styleName) {
        tabContainer.addStyleName(styleName);
    }

    public DialogTab getActiveTab() {
        return activeTab;
    }
}
