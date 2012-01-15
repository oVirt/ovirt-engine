package org.ovirt.engine.ui.uicommonweb.models.tags;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class TagModel extends Model
{

    public static EventDefinition SelectionChangedEventDefinition;
    private Event privateSelectionChangedEvent;

    public Event getSelectionChangedEvent()
    {
        return privateSelectionChangedEvent;
    }

    private void setSelectionChangedEvent(Event value)
    {
        privateSelectionChangedEvent = value;
    }

    public static void RecursiveEditAttachDetachLists(TagModel tagModel,
            java.util.Map<Guid, Boolean> attachedEntities,
            java.util.ArrayList<Guid> tagsToAttach,
            java.util.ArrayList<Guid> tagsToDetach)
    {
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (tagModel.getSelection() != null && tagModel.getSelection().equals(true)
                && (!attachedEntities.containsKey(tagModel.getId()) || attachedEntities.get(tagModel.getId()) == false))
        {
            tagsToAttach.add(tagModel.getId());
        }
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        else if (tagModel.getSelection() != null && tagModel.getSelection().equals(false)
                && attachedEntities.containsKey(tagModel.getId()))
        {
            tagsToDetach.add(tagModel.getId());
        }
        if (tagModel.getChildren() != null)
        {
            for (TagModel subModel : tagModel.getChildren())
            {
                RecursiveEditAttachDetachLists(subModel, attachedEntities, tagsToAttach, tagsToDetach);
            }
        }
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

    private Guid privateId = new Guid();

    public Guid getId()
    {
        return privateId;
    }

    public void setId(Guid value)
    {
        privateId = value;
    }

    private Guid privateParentId = new Guid();

    public Guid getParentId()
    {
        return privateParentId;
    }

    public void setParentId(Guid value)
    {
        privateParentId = value;
    }

    private java.util.ArrayList<TagModel> privateChildren;

    public java.util.ArrayList<TagModel> getChildren()
    {
        return privateChildren;
    }

    public void setChildren(java.util.ArrayList<TagModel> value)
    {
        privateChildren = value;
    }

    private EntityModel privateName;

    public EntityModel getName()
    {
        return privateName;
    }

    public void setName(EntityModel value)
    {
        privateName = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private Boolean selection;

    public Boolean getSelection()
    {
        return selection;
    }

    public void setSelection(Boolean value)
    {
        if (selection == null && value == null)
        {
            return;
        }
        // C# TO JAVA CONVERTER TODO TASK: Comparisons involving nullable type instances are not converted to null-value
        // logic:
        if (selection == null || !selection.equals(value))
        {
            selection = value;
            getSelectionChangedEvent().raise(this, EventArgs.Empty);
            OnPropertyChanged(new PropertyChangedEventArgs("Selection"));
        }
    }

    private TagModelType type = TagModelType.values()[0];

    public TagModelType getType()
    {
        return type;
    }

    public void setType(TagModelType value)
    {
        if (type != value)
        {
            type = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Type"));
        }
    }

    static
    {
        SelectionChangedEventDefinition = new EventDefinition("SelectionChanged", TagModel.class);
    }

    public TagModel()
    {
        setSelectionChangedEvent(new Event(SelectionChangedEventDefinition));

        setName(new EntityModel());
        setDescription(new EntityModel());
    }

    public boolean Validate()
    {
        LengthValidation tempVar = new LengthValidation();
        tempVar.setMaxLength(40);
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setExpression("^[A-Za-z0-9_-]+$");
        tempVar2.setMessage("Name can contain only 'A-Z', 'a-z', '0-9', '_' or '-' characters.");
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        return getName().getIsValid();
    }
}
