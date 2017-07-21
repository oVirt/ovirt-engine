package org.ovirt.engine.ui.common.widget.tab;

import org.ovirt.engine.ui.common.uicommon.model.GroupedTabData;

import com.google.gwt.event.shared.HasHandlers;

public class GroupedTab extends SimpleTab implements HasHandlers {

    public GroupedTab(final GroupedTabData tabData, AbstractTabPanel tabPanel) {
        super(tabData, tabPanel);
        setGroupTitle(tabData.getGroupTitle());
        setGroupPriority(tabData.getGroupPriority());
        setIcon(tabData.getIcon());
    }

}
