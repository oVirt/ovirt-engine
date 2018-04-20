package org.ovirt.engine.ui.uicommonweb.models.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class TagListModel extends SearchableListModel<Void, TagModel> {

    private Map<Guid, Boolean> attachedTagsToEntities;
    private UICommand newCommand;
    private UICommand editCommand;
    private UICommand removeCommand;
    private UICommand resetCommand;
    private List<SelectionTreeNodeModel> selectionNodeList;

    public TagListModel() {
        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setResetCommand(new UICommand("Reset", this)); //$NON-NLS-1$

        setIsTimerDisabled(true);

        getSearchCommand().execute();

        updateActionAvailability();

        // Initialize SelectedItems property with empty collection.
        setSelectedItems(new ArrayList<TagModel>());

        setSelectionNodeList(new ArrayList<SelectionTreeNodeModel>());
    }

    @Override
    protected boolean isSingleSelectionOnly() {
        return true;
    }

    public UICommand getNewCommand() {
        return newCommand;
    }

    private void setNewCommand(UICommand value) {
        newCommand = value;
    }

    @Override
    public UICommand getEditCommand() {
        return editCommand;
    }

    private void setEditCommand(UICommand value) {
        editCommand = value;
    }

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public UICommand getResetCommand() {
        return resetCommand;
    }

    private void setResetCommand(UICommand value) {
        resetCommand = value;
    }

    @Override
    public TagModel getSelectedItem() {
        return super.getSelectedItem();
    }

    public void setSelectedItem(TagModel value) {
        super.setSelectedItem(value);
    }

    @Override
    public void setItems(Collection<TagModel> value) {
        if (items != value) {
            if (items != null && value != null) {
                Set<TagModel> currentSelected = getActiveItems(items.iterator().next());
                updateSelection(value.iterator().next(), currentSelected);
            }
            itemsChanging(value, items);
            items = value;
            itemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    private void updateSelection(TagModel model, Set<TagModel> currentSelected) {
        for (TagModel oldModel: currentSelected) {
            if (model.getName().getEntity().equals(oldModel.getName().getEntity())) {
                model.setSelection(oldModel.getSelection());
            }
        }
        for (TagModel child: model.getChildren()) {
            updateSelection(child, currentSelected);
        }
    }

    private Set<TagModel> getActiveItems(TagModel model) {
        Set<TagModel> result = new HashSet<>();
        if (model.getSelection()) {
            result.add(model);
        }
        for (TagModel child : model.getChildren()) {
            result.addAll(getActiveItems(child));
        }
        return result;
    }

    public List<SelectionTreeNodeModel> getSelectionNodeList() {
        return selectionNodeList;
    }

    public void setSelectionNodeList(List<SelectionTreeNodeModel> value) {
        if (selectionNodeList != value) {
            selectionNodeList = value;
            onPropertyChanged(new PropertyChangedEventArgs("SelectionNodeList")); //$NON-NLS-1$
        }
    }

    public Map<Guid, Boolean> getAttachedTagsToEntities() {
        return attachedTagsToEntities;
    }

    public void setAttachedTagsToEntities(Map<Guid, Boolean> value) {
        if (attachedTagsToEntities != value) {
            attachedTagsToEntities = value;
            attachedTagsToEntitiesChanged();
            onPropertyChanged(new PropertyChangedEventArgs("AttachedTagsToEntities")); //$NON-NLS-1$
        }
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        AsyncDataProvider.getInstance().getRootTag(new AsyncQuery<>(returnValue -> {

            TagModel rootTag = tagToModel(returnValue);
            rootTag.getName().setEntity(ConstantsManager.getInstance().getConstants().rootTag());
            rootTag.setType(TagModelType.Root);
            rootTag.setIsChangeable(false);
            setItems(new ArrayList<>(Arrays.asList(new TagModel[] { rootTag })));

        }));
    }

    @Override
    protected void itemsChanged() {
        super.itemsChanged();

        if (getSelectionNodeList() != null && getSelectionNodeList().isEmpty() && getAttachedTagsToEntities() != null) {
            attachedTagsToEntitiesChanged();
        }
    }

    protected void attachedTagsToEntitiesChanged() {
        ArrayList<TagModel> tags = (ArrayList<TagModel>) getItems();

        if (tags != null) {
            TagModel root = tags.get(0);

            if (getAttachedTagsToEntities() != null) {
                recursiveSetSelection(root, getAttachedTagsToEntities());
            }

            if (getSelectionNodeList().isEmpty()) {
                setSelectionNodeList(new ArrayList<>(Arrays.asList(new SelectionTreeNodeModel[]{createTree(root)})));
            }
        }
    }

    public void recursiveSetSelection(TagModel tagModel, Map<Guid, Boolean> attachedEntities) {
        if (attachedEntities.containsKey(tagModel.getId()) && attachedEntities.get(tagModel.getId())) {
            tagModel.setSelection(true);
        } else {
            tagModel.setSelection(false);
        }
        if (tagModel.getChildren() != null) {
            for (TagModel subModel : tagModel.getChildren()) {
                recursiveSetSelection(subModel, attachedEntities);
            }
        }
    }

    public SelectionTreeNodeModel createTree(TagModel tag) {
        SelectionTreeNodeModel node = new SelectionTreeNodeModel();
        node.setDescription(tag.getName().getEntity());
        node.setIsSelectedNullable(tag.getSelection());
        node.setIsChangeable(tag.getIsChangable());
        node.setIsSelectedNotificationPrevent(true);
        node.setEntity(tag);
        node.getPropertyChangedEvent().addListener(this);

        if (tag.getChildren().isEmpty()) {
            getSelectionNodeList().add(node);
            return node;
        }

        for (TagModel childTag : tag.getChildren()) {
            SelectionTreeNodeModel childNode = createTree(childTag);
            childNode.setParent(node);
            node.getChildren().add(childNode);
        }

        return node;
    }

    public TagModel cloneTagModel(TagModel tag) {
        ArrayList<TagModel> children = new ArrayList<>();
        for (TagModel child : tag.getChildren()) {
            children.add(cloneTagModel(child));
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

        for (TagModel child : children) {
            child.setParent(model);
        }

        return model;
    }

    public TagModel tagToModel(Tags tag) {
        EntityModel<String> tempVar = new EntityModel<>();
        tempVar.setEntity(tag.getTagName());
        EntityModel<String> name = tempVar;
        EntityModel<String> tempVar2 = new EntityModel<>();
        tempVar2.setEntity(tag.getDescription());
        EntityModel<String> description = tempVar2;

        ArrayList<TagModel> children = new ArrayList<>();
        for (Tags a : tag.getChildren()) {
            children.add(tagToModel(a));
        }

        TagModel model = new TagModel();
        model.setId(tag.getTagId());
        model.setName(name);
        model.setDescription(description);
        model.setType((tag.getIsReadonly() == null ? false : tag.getIsReadonly()) ? TagModelType.ReadOnly
                : TagModelType.Regular);
        model.setSelection(false);
        model.setParentId(tag.getParentId() == null ? Guid.Empty : tag.getParentId());
        model.setChildren(children);

        for (TagModel child : children) {
            child.setParent(model);
        }

        model.getSelectionChangedEvent().addListener(this);

        return model;
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(TagModel.selectionChangedEventDefinition)) {
            onTagSelectionChanged(sender, args);
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);
        if (e.propertyName.equals("IsSelectedNullable")) { //$NON-NLS-1$
            SelectionTreeNodeModel selectionTreeNodeModel = (SelectionTreeNodeModel) sender;
            TagModel tagModel = (TagModel) selectionTreeNodeModel.getEntity();

            tagModel.setSelection(selectionTreeNodeModel.getIsSelectedNullable());
            onTagSelectionChanged(tagModel, e);
        }
    }

    private void onTagSelectionChanged(Object sender, EventArgs e) {
        TagModel model = (TagModel) sender;

        List<TagModel> list = new ArrayList<>();
        if (getSelectedItems() != null) {
            for (Object item : getSelectedItems()) {
                list.add((TagModel) item);
            }
        }

        if (model.getSelection() == null ? false : model.getSelection()) {
            list.add(model);
        } else {
            list.remove(model);
        }

        setSelectedItems(list);
    }

    private void reset() {
        setSelectedItems(new ArrayList<TagModel>());

        if (getItems() != null) {
            for (Object item : getItems()) {
                resetInternal((TagModel) item);
            }
        }
    }

    private void resetInternal(TagModel root) {
        root.setSelection(false);
        for (TagModel item : root.getChildren()) {
            resetInternal(item);
        }
    }

    public void remove() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeTagsTitle());
        model.setHelpTag(HelpTag.remove_tag);
        model.setHashName("remove_tag"); //$NON-NLS-1$

        ArrayList<String> items = new ArrayList<>();
        items.add(getSelectedItem().getName().getEntity());
        model.setItems(items);

        model.setNote(ConstantsManager.getInstance().getConstants().noteRemovingTheTagWillAlsoRemoveAllItsDescendants());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onRemove() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();

        Frontend.getInstance().runAction(ActionType.RemoveTag, new TagsActionParametersBase(getSelectedItem().getId()),
                result -> {

                    TagListModel tagListModel = (TagListModel) result.getState();
                    ActionReturnValue returnVal = result.getReturnValue();
                    boolean success = returnVal != null && returnVal.getSucceeded();
                    if (success) {
                        tagListModel.getSearchCommand().execute();
                    }
                    tagListModel.cancel();
                    tagListModel.stopProgress();

                }, this);
    }

    public void edit() {
        if (getWindow() != null) {
            return;
        }

        TagModel model = new TagModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editTagTitle());
        model.setHelpTag(HelpTag.edit_tag);
        model.setHashName("edit_tag"); //$NON-NLS-1$
        model.setIsNew(false);
        model.getName().setEntity(getSelectedItem().getName().getEntity());
        model.getDescription().setEntity(getSelectedItem().getDescription().getEntity());
        model.setParent(getSelectedItem());
        model.setParentId(getSelectedItem().getParentId());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void newEntity() {
        if (getWindow() != null) {
            return;
        }

        TagModel model = new TagModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newTagTitle());
        model.setHelpTag(HelpTag.new_tag);
        model.setHashName("new_tag"); //$NON-NLS-1$
        model.setIsNew(true);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onSave() {
        TagModel model = (TagModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        if (!model.validate()) {
            return;
        }

        Tags tempVar =
                new Tags();
        tempVar.setTagId(model.getIsNew() ? Guid.Empty : getSelectedItem().getId());
        tempVar.setParentId(model.getIsNew() ? getSelectedItem().getId() : getSelectedItem().getParentId());
        tempVar.setTagName(model.getName().getEntity());
        tempVar.setDescription(model.getDescription().getEntity());
        Tags tag = tempVar;

        model.startProgress();

        Frontend.getInstance().runAction(model.getIsNew() ? ActionType.AddTag : ActionType.UpdateTag,
                new TagsOperationParameters(tag),
                result -> {

                    TagListModel localModel = (TagListModel) result.getState();
                    localModel.postOnSave(result.getReturnValue());

                },
                this);
    }

    public void postOnSave(ActionReturnValue returnValue) {
        TagModel model = (TagModel) getWindow();

        model.stopProgress();

        if (returnValue != null && returnValue.getSucceeded()) {
            cancel();
            getSearchCommand().execute();
        }
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        getNewCommand().setIsExecutionAllowed(getSelectedItem() != null);
        getEditCommand().setIsExecutionAllowed(getSelectedItem() != null
                && getSelectedItem().getType() == TagModelType.Regular);
        getRemoveCommand().setIsExecutionAllowed(getSelectedItem() != null
                && getSelectedItem().getType() == TagModelType.Regular);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getResetCommand()) {
            reset();
        } else if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

    @Override
    protected String getListName() {
        return "TagListModel"; //$NON-NLS-1$
    }

    public TagModel getRootNode() {
        return getItems().iterator().next();
    }
}
