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

    @Override
    public List<SystemTreeItemModel> getChildren()
    {
        return privateChildren;
    }

    public void setChildren(List<SystemTreeItemModel> value)
    {
        privateChildren = value;
    }

    public void addChild(SystemTreeItemModel value) {
        privateChildren.add(value);
        if (value != null) {
            value.setParent(this);
        }
    }

    private SystemTreeItemModel privateParent;

    @Override
    public SystemTreeItemModel getParent()
    {
        return privateParent;
    }

    private void setParent(SystemTreeItemModel value)
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
        if (root != null && root.getType() != type)
        {
            if (root.getParent() != null)
            {
                return findAncestor(type, root.getParent());
            }

            return null;
        }

        return root;
    }

    @Override
    public boolean equals(Object other) {
        return equals(other, false);
    }

    public boolean equals(Object other, boolean deepCompare) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != getClass()) {
            return false;
        }
        boolean result = true;
        SystemTreeItemModel otherModel = (SystemTreeItemModel) other;
        if (getChildren().size() > 0 && deepCompare) {
            if (otherModel.getChildren().size() == getChildren().size()) {
                for (int i = 0; i < getChildren().size(); i++) {
                    result = getChildren().get(i).equals(otherModel.getChildren().get(i), deepCompare);
                    if (!result) {
                        break;
                    }
                }
            } else {
                result = false;
            }
        }
        if (result) {
            if (!((otherModel.getEntity() == null && getEntity() == null)
                    || (otherModel.getEntity() != null && otherModel.getEntity().equals(getEntity())
                    && ((otherModel.getTitle() == null && getTitle() == null)
                            || (otherModel.getTitle() != null && otherModel.getTitle().equals(getTitle())))))) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getEntity() == null) ? 0 : getEntity().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        return result;
    }

}
