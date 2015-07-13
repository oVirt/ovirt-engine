package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.validation.EmailValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class EventNotificationModel extends Model {

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

    private EntityModel<String> privateEmail;

    public EntityModel<String> getEmail() {
        return privateEmail;
    }

    private void setEmail(EntityModel<String> value) {
        privateEmail = value;
    }

    private String privateOldEmail;

    public String getOldEmail() {
        return privateOldEmail;
    }

    public void setOldEmail(String value) {
        privateOldEmail = value;
    }

    private ArrayList<SelectionTreeNodeModel> eventGroupModels;

    public ArrayList<SelectionTreeNodeModel> getEventGroupModels() {
        return eventGroupModels;
    }

    public void setEventGroupModels(ArrayList<SelectionTreeNodeModel> value) {
        if ((eventGroupModels == null && value != null)
                || (eventGroupModels != null && !eventGroupModels.equals(value))) {
            eventGroupModels = value;
            onPropertyChanged(new PropertyChangedEventArgs("EventGroupModels")); //$NON-NLS-1$
        }
    }

    public EventNotificationModel() {
        setExpandAllCommand(new UICommand("ExpandAll", this)); //$NON-NLS-1$
        setCollapseAllCommand(new UICommand("CollapseAll", this)); //$NON-NLS-1$

        setEmail(new EntityModel<String>());
    }

    public void expandAll() {
        for (SelectionTreeNodeModel a : getEventGroupModels()) {
            a.setIsExpanded(true);
        }
    }

    public void collapseAll() {
        for (SelectionTreeNodeModel a : getEventGroupModels()) {
            a.setIsExpanded(false);
        }
    }

    public boolean validate() {
        getEmail().validateEntity(new IValidation[] { new NotEmptyValidation(), new EmailValidation() });

        return getEmail().getIsValid();
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getExpandAllCommand()) {
            expandAll();
        }
        if (command == getCollapseAllCommand()) {
            collapseAll();
        }
    }

}
