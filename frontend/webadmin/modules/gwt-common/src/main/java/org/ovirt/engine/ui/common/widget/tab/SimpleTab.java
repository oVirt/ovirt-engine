package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.TabData;

public class SimpleTab extends AbstractCompositeTab {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleTab> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public SimpleTab(TabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

}
