package org.ovirt.engine.ui.common.view;

import java.util.Map;

import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.OvirtTabListItem;
import org.ovirt.engine.ui.uicommonweb.models.TabName;

/**
 * This interface represents all the tabbed views that have side tabs.
 */
public interface TabbedView {
    /**
     * Get a reference to the panel containing all the tabs.
     * @return A {@code DialogTabPanel}.
     */
    DialogTabPanel getTabPanel();

    /**
     * Get a mapping between the tab names and the {@code DialogTab}s.
     * @return A {@code Map} between the names and the {@code DialogTab}s.
     */
    Map<TabName, OvirtTabListItem> getTabNameMapping();
}
