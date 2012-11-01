package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.TabData;

/**
 * Simple factory that abstracts from tab widget creation details.
 */
public abstract class TabFactory {

    public static TabDefinition createTab(TabData tabData, AbstractTabPanel tabPanel, EventBus eventBus) {
        if (tabData instanceof ModelBoundTabData) {
            // Tab widget bound to UiCommon model
            return new ModelBoundTab((ModelBoundTabData) tabData, tabPanel, eventBus);
        } else if (tabData instanceof DynamicTabData) {
            // Tab widget added dynamically during runtime
            return new DynamicTab((DynamicTabData) tabData, tabPanel, eventBus);
        } else {
            // Fall back to default tab widget implementation
            return new SimpleTab(tabData, tabPanel);
        }
    }

}
