package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

import com.google.gwt.dom.client.Style.HasCssName;
import com.gwtplatform.mvp.client.TabDataBasic;

public class GroupedTabData extends TabDataBasic {

    private final String groupTitle;
    private final int groupPriority;
    private final HasCssName icon;

    public GroupedTabData(MenuDetails menuDetails) {
        this(menuDetails.getSecondaryTitle(),
                menuDetails.getPrimaryTitle(),
                menuDetails.getSecondaryPriority(),
                menuDetails.getPrimaryPriority(),
                menuDetails.getIcon());
    }

    public GroupedTabData(String label, String groupTitle, int priority, int groupPriority, HasCssName icon) {
        super(label, priority);
        this.groupTitle = groupTitle;
        this.groupPriority = groupPriority;
        this.icon = icon;
    }

    public GroupedTabData(String label, int priority) {
        this(label, null, priority, -1, null);
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
            .append("label", getLabel()) // $NON-NLS-1$
            .append("priority", getPriority()) // $NON-NLS-1$
            .append("groupTitle", groupTitle) // $NON-NLS-1$
            .append("groupPriority", groupPriority) // $NON-NLS-1$
            .build();
    }

}
