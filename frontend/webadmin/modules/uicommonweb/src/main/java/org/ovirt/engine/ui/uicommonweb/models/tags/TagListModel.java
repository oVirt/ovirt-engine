package org.ovirt.engine.ui.uicommonweb.models.tags;

import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unused")
public class TagListModel extends SearchableListModel
{

    public static EventDefinition ResetRequestedEventDefinition;
    private Event privateResetRequestedEvent;

    public Event getResetRequestedEvent()
    {
        return privateResetRequestedEvent;
    }

    private void setResetRequestedEvent(Event value)
    {
        privateResetRequestedEvent = value;
    }

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private UICommand privateResetCommand;

    public UICommand getResetCommand()
    {
        return privateResetCommand;
    }

    private void setResetCommand(UICommand value)
    {
        privateResetCommand = value;
    }

    @Override
    public TagModel getSelectedItem()
    {
        return (TagModel) super.getSelectedItem();
    }

    public void setSelectedItem(TagModel value)
    {
        super.setSelectedItem(value);
    }

    @Override
    public Iterable getItems()
    {
        return items;
    }

    @Override
    public void setItems(Iterable value)
    {
        if (items != value)
        {
            ItemsChanging(value, items);
            items = value;
            ItemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.Empty);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    private ArrayList<SelectionTreeNodeModel> selectionNodeList;

    public ArrayList<SelectionTreeNodeModel> getSelectionNodeList()
    {
        return selectionNodeList;
    }

    public void setSelectionNodeList(ArrayList<SelectionTreeNodeModel> value)
    {
        if (selectionNodeList != value)
        {
            selectionNodeList = value;
            onPropertyChanged(new PropertyChangedEventArgs("SelectionNodeList")); //$NON-NLS-1$
        }
    }

    private Map<Guid, Boolean> attachedTagsToEntities;

    public Map<Guid, Boolean> getAttachedTagsToEntities()
    {
        return attachedTagsToEntities;
    }

    public void setAttachedTagsToEntities(Map<Guid, Boolean> value)
    {
        if (attachedTagsToEntities != value)
        {
            attachedTagsToEntities = value;
            AttachedTagsToEntitiesChanged();
            onPropertyChanged(new PropertyChangedEventArgs("AttachedTagsToEntities")); //$NON-NLS-1$
        }
    }

    static
    {
        ResetRequestedEventDefinition = new EventDefinition("ResetRequested", SystemTreeModel.class); //$NON-NLS-1$
    }

    public TagListModel()
    {
        setResetRequestedEvent(new Event(ResetRequestedEventDefinition));

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setResetCommand(new UICommand("Reset", this)); //$NON-NLS-1$

        setIsTimerDisabled(true);

        getSearchCommand().Execute();

        UpdateActionAvailability();

        // Initialize SelectedItems property with empty collection.
        setSelectedItems(new ArrayList<TagModel>());

        setSelectionNodeList(new ArrayList<SelectionTreeNodeModel>());
    }

    @Override
    protected void SyncSearch()
    {
        super.SyncSearch();

        AsyncDataProvider.GetRootTag(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        TagListModel tagListModel = (TagListModel) target;
                        TagModel rootTag =
                                tagListModel.TagToModel((org.ovirt.engine.core.common.businessentities.tags) returnValue);
                        rootTag.getName().setEntity(ConstantsManager.getInstance().getConstants().rootTag());
                        rootTag.setType(TagModelType.Root);
                        rootTag.setIsChangable(false);
                        tagListModel.setItems(new ArrayList<TagModel>(Arrays.asList(new TagModel[] {rootTag})));

                    }
                }));
    }

    @Override
    protected void ItemsChanged()
    {
        super.ItemsChanged();

        if (getSelectionNodeList() != null && getSelectionNodeList().isEmpty() && getAttachedTagsToEntities() != null)
        {
            AttachedTagsToEntitiesChanged();
        }
    }

    protected void AttachedTagsToEntitiesChanged()
    {
        ArrayList<TagModel> tags = (ArrayList<TagModel>) getItems();

        if (tags != null)
        {
            TagModel root = tags.get(0);

            if (getAttachedTagsToEntities() != null)
            {
                RecursiveSetSelection(root, getAttachedTagsToEntities());
            }

            if (getSelectionNodeList().isEmpty())
            {
                setSelectionNodeList(new ArrayList<SelectionTreeNodeModel>(Arrays.asList(new SelectionTreeNodeModel[] { CreateTree(root) })));
            }
        }
    }

    public void RecursiveSetSelection(TagModel tagModel, Map<Guid, Boolean> attachedEntities)
    {
        if (attachedEntities.containsKey(tagModel.getId()) && attachedEntities.get(tagModel.getId()))
        {
            tagModel.setSelection(true);
        }
        else
        {
            tagModel.setSelection(false);
        }
        if (tagModel.getChildren() != null)
        {
            for (TagModel subModel : tagModel.getChildren())
            {
                RecursiveSetSelection(subModel, attachedEntities);
            }
        }
    }

    public SelectionTreeNodeModel CreateTree(TagModel tag)
    {
        SelectionTreeNodeModel node = new SelectionTreeNodeModel();
        node.setDescription(tag.getName().getEntity().toString());
        node.setIsSelectedNullable(tag.getSelection());
        node.setIsChangable(tag.getIsChangable());
        node.setIsSelectedNotificationPrevent(true);
        node.setEntity(tag);
        node.getPropertyChangedEvent().addListener(this);

        if (tag.getChildren().isEmpty())
        {
            getSelectionNodeList().add(node);
            return node;
        }

        for (TagModel childTag : tag.getChildren())
        {
            SelectionTreeNodeModel childNode = CreateTree(childTag);
            childNode.setParent(node);
            node.getChildren().add(childNode);
        }

        return node;
    }

    public TagModel CloneTagModel(TagModel tag)
    {
        ArrayList<TagModel> children = new ArrayList<TagModel>();
        for (TagModel child : tag.getChildren())
        {
            children.add(CloneTagModel(child));
        }

        TagModel model = new TagModel();
        model.setId(tag.getId());
        model.setName(tag.getName());
        model.setDescription(tag.getDescription());
        model.setType(tag.getType());
        model.setSelection(tag.getSelection());
        model.setParentId(tag.getParentId());
        model.setChildren(children);
        model.getSelectionChangedEvent().addListener(this);

        return model;
    }

    public TagModel TagToModel(org.ovirt.engine.core.common.businessentities.tags tag)
    {
        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(tag.gettag_name());
        EntityModel name = tempVar;
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity(tag.getdescription());
        EntityModel description = tempVar2;

        ArrayList<TagModel> children = new ArrayList<TagModel>();
        for (org.ovirt.engine.core.common.businessentities.tags a : tag.getChildren())
        {
            children.add(TagToModel(a));
        }

        TagModel model = new TagModel();
        model.setId(tag.gettag_id());
        model.setName(name);
        model.setDescription(description);
        model.setType((tag.getIsReadonly() == null ? false : tag.getIsReadonly()) ? TagModelType.ReadOnly
                : TagModelType.Regular);
        model.setSelection(false);
        model.setParentId(tag.getparent_id() == null ? NGuid.Empty : tag.getparent_id().getValue());
        model.setChildren(children);

        model.getSelectionChangedEvent().addListener(this);

        return model;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(TagModel.SelectionChangedEventDefinition))
        {
            OnTagSelectionChanged(sender, args);
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);
        if (e.PropertyName.equals("IsSelectedNullable")) //$NON-NLS-1$
        {
            SelectionTreeNodeModel selectionTreeNodeModel = (SelectionTreeNodeModel) sender;
            TagModel tagModel = (TagModel) selectionTreeNodeModel.getEntity();

            tagModel.setSelection(selectionTreeNodeModel.getIsSelectedNullable());
            OnTagSelectionChanged(tagModel, e);
        }
    }

    private void OnTagSelectionChanged(Object sender, EventArgs e)
    {
        TagModel model = (TagModel) sender;

        ArrayList<TagModel> list = new ArrayList<TagModel>();
        if (getSelectedItems() != null)
        {
            for (Object item : getSelectedItems())
            {
                list.add((TagModel) item);
            }
        }

        if ((model.getSelection() == null ? false : model.getSelection()))
        {
            list.add(model);
        }
        else
        {
            list.remove(model);
        }

        setSelectedItems(list);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    private void Reset()
    {
        setSelectedItems(new ArrayList<TagModel>());

        if (getItems() != null)
        {
            for (Object item : getItems())
            {
                ResetInternal((TagModel) item);
            }
        }

        // Async tag search will cause tree selection to be cleared
        // Search();

        getResetRequestedEvent().raise(this, EventArgs.Empty);
    }

    private void ResetInternal(TagModel root)
    {
        root.setSelection(false);
        for (TagModel item : root.getChildren())
        {
            ResetInternal(item);
        }
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTagsTitle());
        model.setHashName("remove_tag"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().tagsMsg());

        ArrayList<Object> items = new ArrayList<Object>();
        items.add(getSelectedItem().getName().getEntity());
        model.setItems(items);

        model.setNote(ConstantsManager.getInstance().getConstants().noteRemovingTheTagWillAlsoRemoveAllItsDescendants());

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnRemove()
    {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        model.StartProgress(null);

        Frontend.RunAction(VdcActionType.RemoveTag, new TagsActionParametersBase(getSelectedItem().getId()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        TagListModel tagListModel = (TagListModel) result.getState();
                        VdcReturnValueBase returnVal = result.getReturnValue();
                        boolean success = returnVal != null && returnVal.getSucceeded();
                        if (success)
                        {
                            tagListModel.getSearchCommand().Execute();
                        }
                        tagListModel.Cancel();
                        tagListModel.StopProgress();

                    }
                }, this);
    }

    public void Edit()
    {
        if (getWindow() != null)
        {
            return;
        }

        TagModel model = new TagModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editTagTitle());
        model.setHashName("edit_tag"); //$NON-NLS-1$
        model.setIsNew(false);
        model.getName().setEntity(getSelectedItem().getName().getEntity());
        model.getDescription().setEntity(getSelectedItem().getDescription().getEntity());
        model.setParentId(getSelectedItem().getParentId());

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        TagModel model = new TagModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newTagTitle());
        model.setHashName("new_tag"); //$NON-NLS-1$
        model.setIsNew(true);

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnSave()
    {
        TagModel model = (TagModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        org.ovirt.engine.core.common.businessentities.tags tempVar =
                new org.ovirt.engine.core.common.businessentities.tags();
        tempVar.settag_id(model.getIsNew() ? NGuid.Empty : getSelectedItem().getId());
        tempVar.setparent_id(model.getIsNew() ? getSelectedItem().getId() : getSelectedItem().getParentId());
        tempVar.settag_name((String) model.getName().getEntity());
        tempVar.setdescription((String) model.getDescription().getEntity());
        org.ovirt.engine.core.common.businessentities.tags tag = tempVar;

        model.StartProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddTag : VdcActionType.UpdateTag,
                new TagsOperationParameters(tag),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        TagListModel localModel = (TagListModel) result.getState();
                        localModel.PostOnSave(result.getReturnValue());

                    }
                },
                this);
    }

    public void PostOnSave(VdcReturnValueBase returnValue)
    {
        TagModel model = (TagModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
            getSearchCommand().Execute();
        }
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        getNewCommand().setIsExecutionAllowed(getSelectedItem() != null);
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null
                && getSelectedItem().getType() == TagModelType.Regular);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItem() != null
                && getSelectedItem().getType() == TagModelType.Regular);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getResetCommand())
        {
            Reset();
        }
        else if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TagListModel"; //$NON-NLS-1$
    }
}
