package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent;

import com.google.gwt.event.shared.EventBus;

public class DynamicTab extends SimpleTab {

    public DynamicTab(final DynamicTabData tabData, AbstractTabPanel tabPanel, EventBus eventBus) {
        super(tabData, tabPanel);
        setGroupTitle(tabData.getGroupTitle());
        setGroupPriority(tabData.getGroupPriority());
        setIcon(null); // TODO: get icon css name from ui plugin

        eventBus.addHandler(SetDynamicTabAccessibleEvent.getType(), event -> {
            if (tabData.getHistoryToken().equals(event.getHistoryToken())) {
                setAccessible(event.isTabAccessible());
            }
        });
    }

}
