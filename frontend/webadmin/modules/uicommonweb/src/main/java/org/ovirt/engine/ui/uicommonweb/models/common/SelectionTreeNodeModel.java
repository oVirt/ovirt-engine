package org.ovirt.engine.ui.uicommonweb.models.common;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

@SuppressWarnings("unused")
public class SelectionTreeNodeModel extends EntityModel
{

    private String description;

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String value)
    {
        if (!StringHelper.stringsEqual(description, value))
        {
            description = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Description"));
        }
    }

    private Boolean isSelectedNullable;

    public Boolean getIsSelectedNullable()
    {
        return isSelectedNullable;
    }

    public void setIsSelectedNullable(Boolean value)
    {
        if (isSelectedNullable == null && value == null)
        {
            return;
        }
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (isSelectedNullable == null || !isSelectedNullable.equals(value))
        {
            isSelectedNullable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsSelectedNullable"));
            if (!getIsSelectedNotificationPrevent())
            {
                IsSelectedChanged();
            }
        }
    }

    private boolean isExpanded;

    public boolean getIsExpanded()
    {
        return isExpanded;
    }

    public void setIsExpanded(boolean value)
    {
        isExpanded = value;
        OnPropertyChanged(new PropertyChangedEventArgs("IsExpanded"));
    }

    private boolean isSelectedNotificationPrevent;

    public boolean getIsSelectedNotificationPrevent()
    {
        return isSelectedNotificationPrevent;
    }

    public void setIsSelectedNotificationPrevent(boolean value)
    {
        if (isSelectedNotificationPrevent != value)
        {
            isSelectedNotificationPrevent = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsSelectedNotificationPrevent"));
        }
    }

    private SelectionTreeNodeModel parent;

    public SelectionTreeNodeModel getParent()
    {
        return parent;
    }

    public void setParent(SelectionTreeNodeModel value)
    {
        if (parent != value)
        {
            parent = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Parent"));
        }
    }

    private java.util.ArrayList<SelectionTreeNodeModel> children;

    public java.util.ArrayList<SelectionTreeNodeModel> getChildren()
    {
        return children;
    }

    public void setChildren(java.util.ArrayList<SelectionTreeNodeModel> value)
    {
        if ((children == null && value != null) || (children != null && !children.equals(value)))
        {
            children = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Children"));
        }
    }

    private String tooltip;

    public String getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(String value)
    {
        if (!StringHelper.stringsEqual(tooltip, value))
        {
            tooltip = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Tooltip"));
        }
    }

    public SelectionTreeNodeModel()
    {
        setChildren(new java.util.ArrayList<SelectionTreeNodeModel>());
    }

    public void IsSelectedChanged()
    {
        if (getParent() != null)
        {
            UpdateParentSelection();
        }
        // Children.Each(a => a.IsSelectedNotificationPrevent = true);
        // Children.Each(a => a.IsSelected = IsSelected);
        // Children.Each(a => a.IsSelectedNotificationPrevent = false);
        for (SelectionTreeNodeModel child : getChildren())
        {
            child.setIsSelectedNotificationPrevent(true);
            child.setIsSelectedNullable(getIsSelectedNullable());
            child.setIsSelectedNotificationPrevent(false);
            for (SelectionTreeNodeModel grandChild : child.getChildren())
            {
                grandChild.setIsSelectedNotificationPrevent(true);
                grandChild.setIsSelectedNullable(getIsSelectedNullable());
                grandChild.setIsSelectedNotificationPrevent(false);
            }
        }
    }

    public void UpdateParentSelection()
    {
        // int selCount = Parent.Children.Count(a => a.IsSelected == true);
        if (getParent() == null || getParent().getChildren() == null)
        {
            return;
        }
        int selCount = 0, nullCount = 0;
        for (SelectionTreeNodeModel a : getParent().getChildren())
        {
            // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to
            // null-value logic:
            if (a.getIsSelectedNullable() != null && a.getIsSelectedNullable().equals(true))
            {
                selCount += 1;
            }
            else if (a.isSelectedNullable == null)
            {
                nullCount++;
            }

        }

        getParent().setIsSelectedNotificationPrevent(true);
        if (selCount == 0 && nullCount == 0)
        {
            getParent().setIsSelectedNullable(false);
        }
        else
        {
            // if (Parent.Children.Count() == selCount)
            if (getParent().getChildren().size() == selCount)
            {
                getParent().setIsSelectedNullable(true);
            }
            else
            {
                getParent().setIsSelectedNullable(null);
            }
        }
        getParent().setIsSelectedNotificationPrevent(false);

        getParent().UpdateParentSelection();
    }
}
