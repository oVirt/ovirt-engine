package org.ovirt.engine.ui.uicommonweb.models;

import java.util.List;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.uicommonweb.TreeNodeInfo;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class SystemTreeItemModel extends EntityModel implements TreeNodeInfo {

    private SystemTreeItemType type = SystemTreeItemType.values()[0];
    private ApplicationMode applicationMode = ApplicationMode.AllModes;

    public ApplicationMode getApplicationMode() {
        return applicationMode;
    }

    public void setApplicationMode(ApplicationMode applicationMode) {
        this.applicationMode = applicationMode;
    }

    public SystemTreeItemType getType()
    {
        return type;
    }

    public void setType(SystemTreeItemType value)
    {
        if (type != value)
        {
            type = value;
            onPropertyChanged(new PropertyChangedEventArgs("Type")); //$NON-NLS-1$
        }
    }

    private List<SystemTreeItemModel> privateChildren;

    public List<SystemTreeItemModel> getChildren()
    {
        return privateChildren;
    }

    public void setChildren(List<SystemTreeItemModel> value)
    {
        privateChildren = value;
    }

    private SystemTreeItemModel privateParent;

    public SystemTreeItemModel getParent()
    {
        return privateParent;
    }

    public void setParent(SystemTreeItemModel value)
    {
        privateParent = value;
    }

    private boolean isExpanded;

    public boolean getIsExpanded()
    {
        return isExpanded;
    }

    public void setIsExpanded(boolean value)
    {
        if (isExpanded != value)
        {
            isExpanded = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsExpanded")); //$NON-NLS-1$
        }
    }

    public SystemTreeItemModel()
    {
        setChildren(new ObservableCollection<SystemTreeItemModel>());
    }

    public static SystemTreeItemModel findAncestor(SystemTreeItemType type, SystemTreeItemModel root)
    {
        if (root.getType() != type)
        {
            if (root.getParent() != null)
            {
                return findAncestor(type, root.getParent());
            }

            return null;
        }

        return root;
    }

}
