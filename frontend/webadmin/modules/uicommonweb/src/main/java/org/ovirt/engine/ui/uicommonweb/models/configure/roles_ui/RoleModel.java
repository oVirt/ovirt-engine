package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class RoleModel extends Model
{

    private UICommand privateExpandAllCommand;

    public UICommand getExpandAllCommand()
    {
        return privateExpandAllCommand;
    }

    private void setExpandAllCommand(UICommand value)
    {
        privateExpandAllCommand = value;
    }

    private UICommand privateCollapseAllCommand;

    public UICommand getCollapseAllCommand()
    {
        return privateCollapseAllCommand;
    }

    private void setCollapseAllCommand(UICommand value)
    {
        privateCollapseAllCommand = value;
    }

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private EntityModel privateIsAdminRole;

    public EntityModel getIsAdminRole()
    {
        return privateIsAdminRole;
    }

    public void setIsAdminRole(EntityModel value)
    {
        privateIsAdminRole = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    private void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    private void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private java.util.ArrayList<SelectionTreeNodeModel> permissionGroupModels;

    public java.util.ArrayList<SelectionTreeNodeModel> getPermissionGroupModels()
    {
        return permissionGroupModels;
    }

    public void setPermissionGroupModels(java.util.ArrayList<SelectionTreeNodeModel> value)
    {
        if (permissionGroupModels != value)
        {
            permissionGroupModels = value;
            OnPropertyChanged(new PropertyChangedEventArgs("PermissionGroupModels"));
        }
    }

    public RoleModel()
    {
        setExpandAllCommand(new UICommand("ExpandAll", this));
        setCollapseAllCommand(new UICommand("CollapseAll", this));

        setName(new EntityModel());
        setDescription(new EntityModel());
        setIsAdminRole(new EntityModel());
    }

    public void ExpandAll()
    {
        // PermissionGroupModels.Each(a => a.IsExpanded = true );
        for (SelectionTreeNodeModel stm : getPermissionGroupModels())
        {
            stm.setIsExpanded(true);
            for (SelectionTreeNodeModel stmChild : stm.getChildren())
            {
                stmChild.setIsExpanded(true);
            }
        }
    }

    public void CollapseAll()
    {
        // PermissionGroupModels.Each(a => a.IsExpanded = false);
        for (SelectionTreeNodeModel stm : getPermissionGroupModels())
        {
            stm.setIsExpanded(false);
            for (SelectionTreeNodeModel stmChild : stm.getChildren())
            {
                stmChild.setIsExpanded(false);
            }
        }
    }

    public boolean Validate()
    {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^\\w.{0,125}$");
        tempVar.setMessage("Name must be up to 126 characters and start with any word character.");
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setExpression("^[A-Za-z0-9_-]+$");
        tempVar2.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.");
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });
        LengthValidation lengthValidation = new LengthValidation();
        lengthValidation.setMaxLength(4000);
        getDescription().ValidateEntity(new IValidation[] { lengthValidation });

        return getName().getIsValid() && getDescription().getIsValid();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getExpandAllCommand())
        {
            ExpandAll();
        }
        else if (command == getCollapseAllCommand())
        {
            CollapseAll();
        }
    }
}
