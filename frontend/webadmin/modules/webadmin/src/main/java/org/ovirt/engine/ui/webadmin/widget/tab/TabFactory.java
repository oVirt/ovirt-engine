package org.ovirt.engine.ui.webadmin.widget.tab;

import org.ovirt.engine.ui.common.widget.tab.AbstractTabPanel;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.common.widget.tab.TabDefinition;

import com.gwtplatform.mvp.client.TabData;

/**
 * Simple factory that abstracts from tab widget creation details.
 */
public abstract class TabFactory {

    public static TabDefinition createTab(TabData tabData, AbstractTabPanel tabPanel) {
        if (tabData instanceof ModelBoundTabData) {
            return new ModelBoundTab((ModelBoundTabData) tabData, tabPanel);
        } else {
            // Fall back to default tab widget implementation
            return new SimpleTab(tabData, tabPanel);
        }
    }

}
