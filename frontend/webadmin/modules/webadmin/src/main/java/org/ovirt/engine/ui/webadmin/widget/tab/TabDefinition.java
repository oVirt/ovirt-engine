package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.HasAccess;

import com.gwtplatform.mvp.client.Tab;

/**
 * Describes a tab rendered within a {@link com.gwtplatform.mvp.client.TabPanel}.
 */
public interface TabDefinition extends Tab, HasAccess {

    /**
     * Sets the horizontal alignment of the tab.
     */
    void setAlign(Align align);

}
