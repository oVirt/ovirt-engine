package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.gwtplatform.mvp.client.TabData;

/**
 * Base class used to implement composite tab widgets.
 */
public abstract class AbstractTab extends Composite implements TabDefinition {

    // Tab widgets are accessible by default
    public static final boolean DEFAULT_ACCESSIBLE = true;

    protected final float priority;
    protected final AbstractTabPanel tabPanel;

    protected boolean accessible = DEFAULT_ACCESSIBLE;

    @UiField
    public AnchorElement hyperlink;

    public AbstractTab(TabData tabData, AbstractTabPanel tabPanel) {
        this.priority = tabData.getPriority();
        this.tabPanel = tabPanel;
    }

    @Override
    public float getPriority() {
        return priority;
    }

    @Override
    public void setTargetHistoryToken(String historyToken) {
        hyperlink.setHref("#" + historyToken); //$NON-NLS-1$
    }

    @Override
    public boolean isAccessible() {
        return accessible;
    }

    @Override
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
        tabPanel.updateTab(this);
    }
}
