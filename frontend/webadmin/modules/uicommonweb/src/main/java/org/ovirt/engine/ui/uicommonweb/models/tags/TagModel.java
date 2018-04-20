package org.ovirt.engine.ui.uicommonweb.models.tags;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.TreeNodeInfo;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TagModel extends Model implements TreeNodeInfo {

    public static final EventDefinition selectionChangedEventDefinition;
    private Event<EventArgs> privateSelectionChangedEvent;

    public Event<EventArgs> getSelectionChangedEvent() {
        return privateSelectionChangedEvent;
    }

    private void setSelectionChangedEvent(Event<EventArgs> value) {
        privateSelectionChangedEvent = value;
    }

    public static void recursiveEditAttachDetachLists(TagModel tagModel,
            Map<Guid, Boolean> attachedEntities,
            ArrayList<Guid> tagsToAttach,
            ArrayList<Guid> tagsToDetach) {
        if (tagModel.getSelection() != null && tagModel.getSelection()
                && (!attachedEntities.containsKey(tagModel.getId()) || !attachedEntities.get(tagModel.getId()))) {
            tagsToAttach.add(tagModel.getId());
        } else if (tagModel.getSelection() != null && !tagModel.getSelection()
                && attachedEntities.containsKey(tagModel.getId())) {
            tagsToDetach.add(tagModel.getId());
        }
        if (tagModel.getChildren() != null) {
            for (TagModel subModel : tagModel.getChildren()) {
                recursiveEditAttachDetachLists(subModel, attachedEntities, tagsToAttach, tagsToDetach);
            }
        }
    }

    private boolean privateIsNew;

    public boolean getIsNew() {
        return privateIsNew;
    }

    public void setIsNew(boolean value) {
        privateIsNew = value;
    }

    private Guid privateId = Guid.Empty;

    public Guid getId() {
        return privateId;
    }

    public void setId(Guid value) {
        privateId = value;
    }

    private Guid privateParentId = Guid.Empty;

    public Guid getParentId() {
        return privateParentId;
    }

    public void setParentId(Guid value) {
        privateParentId = value;
    }

    private TagModel privateParent;

    public TagModel getParent() {
        return privateParent;
    }

    public void setParent(TagModel value) {
        privateParent = value;
    }

    private ArrayList<TagModel> privateChildren;

    public ArrayList<TagModel> getChildren() {
        return privateChildren;
    }

    public void setChildren(ArrayList<TagModel> value) {
        privateChildren = value;
    }

    private EntityModel<String> privateName;

    public EntityModel<String> getName() {
        return privateName;
    }

    public void setName(EntityModel<String> value) {
        privateName = value;
    }

    private EntityModel<String> privateDescription;

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    public void setDescription(EntityModel<String> value) {
        privateDescription = value;
    }

    private Boolean selection;

    public Boolean getSelection() {
        return selection;
    }

    public void setSelection(Boolean value) {
        if (selection == null && value == null) {
            return;
        }
        if (selection == null || !selection.equals(value)) {
            selection = value;
            getSelectionChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Selection")); //$NON-NLS-1$
        }
    }

    private TagModelType type = TagModelType.values()[0];

    public TagModelType getType() {
        return type;
    }

    public void setType(TagModelType value) {
        if (type != value) {
            type = value;
            onPropertyChanged(new PropertyChangedEventArgs("Type")); //$NON-NLS-1$
        }
    }

    static {
        selectionChangedEventDefinition = new EventDefinition("SelectionChanged", TagModel.class); //$NON-NLS-1$
    }

    public TagModel() {
        setSelectionChangedEvent(new Event<>(selectionChangedEventDefinition));

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
    }

    public boolean validate() {
        LengthValidation tempVar = new LengthValidation();
        tempVar.setMaxLength(40);
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, new I18NNameValidation() });

        return getName().getIsValid();
    }

    @Override
    public void cleanup() {
        cleanupEvents(getSelectionChangedEvent());
        super.cleanup();
    }
}
