package org.ovirt.engine.ui.uicommonweb.models.common;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class SelectionTreeNodeModel extends EntityModel {

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        if (!Objects.equals(description, value)) {
            description = value;
            onPropertyChanged(new PropertyChangedEventArgs("Description")); //$NON-NLS-1$
        }
    }

    private Boolean isSelectedNullable;

    public Boolean getIsSelectedNullable() {
        return isSelectedNullable;
    }

    public void setIsSelectedNullable(Boolean value) {
        if (isSelectedNullable == null && value == null) {
            return;
        }
        if (isSelectedNullable == null || !isSelectedNullable.equals(value)) {
            isSelectedNullable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSelectedNullable")); //$NON-NLS-1$
            if (!getIsSelectedNotificationPrevent()) {
                isSelectedChanged();
            }
        }
    }

    private boolean isExpanded;

    public boolean getIsExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(boolean value) {
        isExpanded = value;
        onPropertyChanged(new PropertyChangedEventArgs("IsExpanded")); //$NON-NLS-1$
    }

    private boolean isSelectedNotificationPrevent;

    public boolean getIsSelectedNotificationPrevent() {
        return isSelectedNotificationPrevent;
    }

    public void setIsSelectedNotificationPrevent(boolean value) {
        if (isSelectedNotificationPrevent != value) {
            isSelectedNotificationPrevent = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSelectedNotificationPrevent")); //$NON-NLS-1$
        }
    }

    private SelectionTreeNodeModel parent;

    public SelectionTreeNodeModel getParent() {
        return parent;
    }

    public void setParent(SelectionTreeNodeModel value) {
        if (parent != value) {
            parent = value;
            onPropertyChanged(new PropertyChangedEventArgs("Parent")); //$NON-NLS-1$
        }
    }

    private ArrayList<SelectionTreeNodeModel> children;

    public ArrayList<SelectionTreeNodeModel> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<SelectionTreeNodeModel> value) {
        if ((children == null && value != null) || (children != null && !children.equals(value))) {
            children = value;
            onPropertyChanged(new PropertyChangedEventArgs("Children")); //$NON-NLS-1$
        }
    }

    private String tooltip;

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String value) {
        if (!Objects.equals(tooltip, value)) {
            tooltip = value;
            onPropertyChanged(new PropertyChangedEventArgs("Tooltip")); //$NON-NLS-1$
        }
    }

    public SelectionTreeNodeModel() {
        setChildren(new ArrayList<SelectionTreeNodeModel>());
    }

    public void isSelectedChanged() {
        if (getParent() != null) {
            updateParentSelection();
        }
        // Children.Each(a => a.IsSelectedNotificationPrevent = true);
        // Children.Each(a => a.IsSelected = IsSelected);
        // Children.Each(a => a.IsSelectedNotificationPrevent = false);
        for (SelectionTreeNodeModel child : getChildren()) {
            child.setIsSelectedNotificationPrevent(true);
            child.setIsSelectedNullable(getIsSelectedNullable());
            child.setIsSelectedNotificationPrevent(false);
            for (SelectionTreeNodeModel grandChild : child.getChildren()) {
                grandChild.setIsSelectedNotificationPrevent(true);
                grandChild.setIsSelectedNullable(getIsSelectedNullable());
                grandChild.setIsSelectedNotificationPrevent(false);
            }
        }
    }

    public void updateParentSelection() {
        // int selCount = Parent.Children.Count(a => a.IsSelected == true);
        if (getParent() == null || getParent().getChildren() == null) {
            return;
        }
        int selCount = 0;
        int nullCount = 0;
        for (SelectionTreeNodeModel a : getParent().getChildren()) {
            if (a.getIsSelectedNullable() != null && a.getIsSelectedNullable().equals(true)) {
                selCount += 1;
            } else if (a.isSelectedNullable == null) {
                nullCount++;
            }

        }

        getParent().setIsSelectedNotificationPrevent(true);
        if (selCount == 0 && nullCount == 0) {
            getParent().setIsSelectedNullable(false);
        } else {
            // if (Parent.Children.Count() == selCount)
            if (getParent().getChildren().size() == selCount) {
                getParent().setIsSelectedNullable(true);
            } else {
                getParent().setIsSelectedNullable(null);
            }
        }
        getParent().setIsSelectedNotificationPrevent(false);

        getParent().updateParentSelection();
    }
}
