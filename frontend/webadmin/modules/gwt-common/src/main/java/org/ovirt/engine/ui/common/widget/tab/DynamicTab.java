package org.ovirt.engine.ui.common.widget.tab;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent;

import com.google.gwt.event.shared.EventBus;

public class DynamicTab extends SimpleTab {

    public DynamicTab(final DynamicTabData tabData, AbstractTabPanel tabPanel, EventBus eventBus) {
        super(tabData, tabPanel);
        setGroupTitle(tabData.getGroupTitle());
        setGroupPriority(tabData.getGroupPriority());
        setIcon(null); // TODO: get icon css name from ui plugin
        // THIS IS A HACK FIX ME WHEN YOU FIX THE ABOVE TODO
        if (getPriority() == -1) {
            // Assume we are the dashboard, but it should come from the plugin itself.
            setIcon(IconType.TACHOMETER);
        }

        eventBus.addHandler(SetDynamicTabAccessibleEvent.getType(), event -> {
            if (tabData.getHistoryToken().equals(event.getHistoryToken())) {
                setAccessible(event.isTabAccessible());
            }
        });
    }

}
