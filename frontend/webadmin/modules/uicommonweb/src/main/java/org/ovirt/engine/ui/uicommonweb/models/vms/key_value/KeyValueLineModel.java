package org.ovirt.engine.ui.uicommonweb.models.vms.key_value;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class KeyValueLineModel extends EntityModel {
    ListModel keys;

    ListModel values;
    EntityModel value;

    private UICommand removeLine;
    private UICommand addLine;

    private final IModifyLines modifyLines;

    public UICommand getRemoveLine() {
        return removeLine;
    }

    public void setRemoveLine(UICommand removeLine) {
        this.removeLine = removeLine;
    }

    public UICommand getAddLine() {
        return addLine;
    }

    public void setAddLine(UICommand addLine) {
        this.addLine = addLine;
    }

    public ListModel getKeys() {
        return keys;
    }

    public void setKeys(ListModel keys) {
        this.keys = keys;
    }

    public ListModel getValues() {
        return values;
    }

    public void setValues(ListModel values) {
        this.values = values;
    }

    public EntityModel getValue() {
        return value;
    }

    public void setValue(EntityModel value) {
        this.value = value;
    }

    public KeyValueLineModel(IModifyLines modifyLines) {
        this.modifyLines = modifyLines;
        setKeys(new ListModel());
        setValue(new EntityModel());
        setValues(new ListModel());
        getValues().setIsAvailable(false);
        setRemoveLine(new UICommand("remove", this)); //$NON-NLS-1$
        setAddLine(new UICommand("add", this)); //$NON-NLS-1$
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if (command.equals(getAddLine())) {
            modifyLines.addLine(this);
        }
        else if (command.equals(getRemoveLine())) {
            modifyLines.removeLine(this);
        }
    }
}
