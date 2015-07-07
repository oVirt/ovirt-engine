package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractHeadlessTabPanel;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;
import org.ovirt.engine.ui.common.widget.tab.TabFactory;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class HeadlessTabPanel extends AbstractHeadlessTabPanel {

    interface WidgetUiBinder extends UiBinder<Widget, HeadlessTabPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public HeadlessTabPanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected TabDefinition createNewTab(TabData tabData) {
        return TabFactory.createTab(tabData, this, ClientGinjectorProvider.getEventBus());
    }

}
