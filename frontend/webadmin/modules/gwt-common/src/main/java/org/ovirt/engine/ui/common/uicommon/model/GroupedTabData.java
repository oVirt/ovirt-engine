package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

import com.google.gwt.dom.client.Style.HasCssName;
import com.gwtplatform.mvp.client.TabDataBasic;

public class GroupedTabData extends TabDataBasic {
    private final String groupTitle;
    private final int groupPriority;
    private final HasCssName icon;

    public GroupedTabData(String label, String groupTitle, int priority, int groupPriority, HasCssName icon) {
        super(label, priority);
        this.groupTitle = groupTitle;
        this.groupPriority = groupPriority;
        this.icon = icon;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public int getGroupPriority() {
        return groupPriority;
    }

    public HasCssName getIcon() {
        return icon;
    }

    public String toString() {
        return ToStringBuilder.forInstance(this)
            .append("Group", groupTitle) // $NON-NLS-1$
            .append("label", getLabel()) // $NON-NLS-1$
            .append("priority", getPriority()).toString(); // $NON-NLS-1$
    }

}
