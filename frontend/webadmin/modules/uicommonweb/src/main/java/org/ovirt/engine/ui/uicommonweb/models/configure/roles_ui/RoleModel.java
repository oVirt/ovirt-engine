package org.ovirt.engine.ui.uicommonweb.models.configure.roles_ui;

import java.util.ArrayList;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class RoleModel extends Model {

    private UICommand privateExpandAllCommand;

    public UICommand getExpandAllCommand() {
        return privateExpandAllCommand;
    }

    private void setExpandAllCommand(UICommand value) {
        privateExpandAllCommand = value;
    }

    private UICommand privateCollapseAllCommand;

    public UICommand getCollapseAllCommand() {
        return privateCollapseAllCommand;
    }

    private void setCollapseAllCommand(UICommand value) {
        privateCollapseAllCommand = value;
    }

    private boolean privateIsNew;

    public boolean getIsNew() {
        return privateIsNew;
    }

    public void setIsNew(boolean value) {
        privateIsNew = value;
    }

    private EntityModel<Boolean> privateIsAdminRole;

    public EntityModel<Boolean> getIsAdminRole() {
        return privateIsAdminRole;
    }

    public void setIsAdminRole(EntityModel<Boolean> value) {
        privateIsAdminRole = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    private void setName(EntityModel<String> value) {
        privateName = value;
    }

    private EntityModel<String> privateDescription;

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    private void setDescription(EntityModel<String> value) {
        privateDescription = value;
    }

    private ArrayList<SelectionTreeNodeModel> permissionGroupModels;

    public ArrayList<SelectionTreeNodeModel> getPermissionGroupModels() {
        return permissionGroupModels;
    }

    public void setPermissionGroupModels(ArrayList<SelectionTreeNodeModel> value) {
        if (permissionGroupModels != value) {
            permissionGroupModels = value;
            onPropertyChanged(new PropertyChangedEventArgs("PermissionGroupModels")); //$NON-NLS-1$
        }
    }

    public RoleModel() {
        setExpandAllCommand(new UICommand("ExpandAll", this)); //$NON-NLS-1$
        setCollapseAllCommand(new UICommand("CollapseAll", this)); //$NON-NLS-1$

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setIsAdminRole(new EntityModel<Boolean>());
    }

    public void expandAll() {
        // PermissionGroupModels.Each(a => a.IsExpanded = true );
        for (SelectionTreeNodeModel stm : getPermissionGroupModels()) {
            stm.setIsExpanded(true);
            for (SelectionTreeNodeModel stmChild : stm.getChildren()) {
                stmChild.setIsExpanded(true);
            }
        }
    }

    public void collapseAll() {
        // PermissionGroupModels.Each(a => a.IsExpanded = false);
        for (SelectionTreeNodeModel stm : getPermissionGroupModels()) {
            stm.setIsExpanded(false);
            for (SelectionTreeNodeModel stmChild : stm.getChildren()) {
                stmChild.setIsExpanded(false);
            }
        }
    }

    public boolean validate() {
        RegexValidation tempVar = new RegexValidation();
        tempVar.setExpression("^\\w.{0,125}$"); //$NON-NLS-1$
        tempVar.setMessage(ConstantsManager.getInstance().getConstants().nameMustBeUpToAndStartWithMsg());
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setExpression("^[A-Za-z0-9_-]+$"); //$NON-NLS-1$
        tempVar2.setMessage(ConstantsManager.getInstance().getConstants().asciiNameValidationMsg());
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });
        LengthValidation lengthValidation = new LengthValidation();
        lengthValidation.setMaxLength(4000);
        getDescription().validateEntity(new IValidation[] { lengthValidation });

        return getName().getIsValid() && getDescription().getIsValid();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getExpandAllCommand()) {
            expandAll();
        } else if (command == getCollapseAllCommand()) {
            collapseAll();
        }
    }
}
