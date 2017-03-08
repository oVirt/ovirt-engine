package org.ovirt.engine.ui.userportal.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabFactory;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.Tab;
import com.gwtplatform.mvp.client.TabData;

public class SimpleTabPanel extends AbstractTabPanel {
    interface WidgetUiBinder extends UiBinder<Widget, SimpleTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface AnchorText extends SafeHtmlTemplates {
        @Template("<a href=\"{1}\">{0}</a>")
        SafeHtml anchor(SafeHtml text, String hashTag);
    }

    @UiField
    FlowPanel tabContainer;

    public SimpleTabPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void setTabBar(IsWidget widget) {
        tabContainer.clear();
        if (widget != null) {
            tabContainer.add(widget);
        }
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return TabFactory.createTab(tabData, this, ClientGinjectorProvider.getEventBus());
    }

    @Override
    public void addTabDefinition(Tab tab, int index) {
    }

    @Override
    public void removeTabDefinition(Tab tab) {
    }

    @Override
    public Tab addTab(TabData tabData, String historyToken) {
        return null;
    }
}
