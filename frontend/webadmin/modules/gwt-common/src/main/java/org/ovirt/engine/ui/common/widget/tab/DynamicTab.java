package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.presenter.SetDynamicTabAccessibleEvent;
import com.google.gwt.event.shared.EventBus;

public class DynamicTab extends SimpleTab {

    public DynamicTab(final DynamicTabData tabData, AbstractTabPanel tabPanel, EventBus eventBus) {
        super(tabData, tabPanel);
        setAlign(tabData.getAlign());

        eventBus.addHandler(SetDynamicTabAccessibleEvent.getType(), event -> {
            if (tabData.getHistoryToken().equals(event.getHistoryToken())) {
                setAccessible(event.isTabAccessible());
            }
        });
    }

}
