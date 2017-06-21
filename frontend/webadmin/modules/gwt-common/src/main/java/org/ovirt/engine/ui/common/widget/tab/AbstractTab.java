package org.ovirt.engine.ui.common.widget.tab;

import com.google.gwt.dom.client.Style.HasCssName;
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

    private String groupTitle;
    private int groupPriority;
    private HasCssName icon;

    private String href;
    private String id;

    public AbstractTab(TabData tabData, AbstractTabPanel tabPanel) {
        this.priority = tabData.getPriority();
        this.tabPanel = tabPanel;
    }

    @Override
    public float getPriority() {
        return priority;
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

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupTitle() {
        return this.groupTitle;
    }

    public void setGroupPriority(int priority) {
        groupPriority = priority;
    }

    public int getGroupPriority() {
        return groupPriority;
    }

    public HasCssName getIcon() {
        return icon;
    }

    public void setIcon(HasCssName icon) {
        this.icon = icon;
    }

    @Override
    public void setTargetHistoryToken(String historyToken) {
        href = "#" + historyToken; //$NON-NLS-1$
    }

    public String getTargetHistoryToken() {
        return href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
