package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.HyperlinkTab;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class HyperlinkTabPanel extends SimpleTabPanel {
    interface WidgetUiBinder extends UiBinder<Widget, HyperlinkTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @Override
    protected void initWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return new HyperlinkTab(tabData, this);
    }
}
