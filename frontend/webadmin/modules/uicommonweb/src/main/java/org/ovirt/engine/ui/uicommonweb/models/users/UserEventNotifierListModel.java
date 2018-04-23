package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.Translator;

@SuppressWarnings("unused")
public class UserEventNotifierListModel extends SearchableListModel<DbUser, EventSubscriber> {

    private UICommand privateManageEventsCommand;

    public UICommand getManageEventsCommand() {
        return privateManageEventsCommand;
    }

    private void setManageEventsCommand(UICommand value) {
        privateManageEventsCommand = value;
    }

    public UserEventNotifierListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().eventNotifierTitle());
        setHelpTag(HelpTag.event_notifier);
        setHashName("event_notifier"); //$NON-NLS-1$

        setManageEventsCommand(new UICommand("ManageEvents", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        super.syncSearch(QueryType.GetEventSubscribersBySubscriberIdGrouped,
                new IdQueryParameters(getEntity().getId()));
    }

    public void manageEvents() {
        EventNotificationModel model = new EventNotificationModel();
        setWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().addEventNotificationTitle());
        model.setHelpTag(HelpTag.add_event_notification);
        model.setHashName("add_event_notification"); //$NON-NLS-1$

        List<EventNotificationEntity> eventTypes = ApplicationModeHelper.getModeSpecificEventNotificationTypeList();
        Map<EventNotificationEntity, Set<AuditLogType>> availableEvents =
                AsyncDataProvider.getInstance().getAvailableNotificationEvents();

        Translator translator = EnumTranslator.getInstance();

        ArrayList<SelectionTreeNodeModel> list = new ArrayList<>();

        Collection<EventSubscriber> items = getItems() == null ? new ArrayList<EventSubscriber>() : getItems();
        for (EventNotificationEntity eventType : eventTypes) {
            SelectionTreeNodeModel stnm = new SelectionTreeNodeModel();
            stnm.setTitle(eventType.toString());
            stnm.setDescription(translator.translate(eventType));
            list.add(stnm);

            for (AuditLogType logtype : availableEvents.get(eventType)) {
                SelectionTreeNodeModel eventGrp = new SelectionTreeNodeModel();

                String description;
                try {
                    description = translator.translate(logtype);
                } catch (MissingResourceException e) {
                    description = logtype.toString();
                }

                eventGrp.setTitle(logtype.toString());
                eventGrp.setDescription(description);
                eventGrp.setParent(list.get(list.size() - 1));
                eventGrp.setIsSelectedNotificationPrevent(true);
                eventGrp.setIsSelectedNullable(false);
                for (EventSubscriber es : items) {
                    if (es.getEventUpName().equals(logtype.toString())) {
                        eventGrp.setIsSelectedNullable(true);
                        break;
                    }
                }

                list.get(list.size() - 1).getChildren().add(eventGrp);
                eventGrp.setIsSelectedNotificationPrevent(false);
            }
            if (list.get(list.size() - 1).getChildren().size() > 0) {
                list.get(list.size() - 1).getChildren().get(0).updateParentSelection();
            }
        }

        model.setEventGroupModels(list);
        if (!StringHelper.isNullOrEmpty(getEntity().getEmail())) {
            model.getEmail().setEntity(getEntity().getEmail());
        } else if (items.size() > 0) {
            model.getEmail().setEntity(items.iterator().next().getMethodAddress());
        }

        model.setOldEmail(model.getEmail().getEntity());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onSave() {
        EventNotificationModel model = (EventNotificationModel) getWindow();

        if (!model.validate()) {
            return;
        }

        ArrayList<ActionParametersBase> toAddList = new ArrayList<>();
        ArrayList<ActionParametersBase> toRemoveList = new ArrayList<>();

        // var selected = model.EventGroupModels.SelectMany(a => a.Children).Where(a => a.IsSelected == true);
        ArrayList<SelectionTreeNodeModel> selected = new ArrayList<>();
        for (SelectionTreeNodeModel node : model.getEventGroupModels()) {
            for (SelectionTreeNodeModel child : node.getChildren()) {
                if (child.getIsSelectedNullable() != null && child.getIsSelectedNullable().equals(true)) {
                    selected.add(child);
                }
            }
        }

        Collection<EventSubscriber> existing = getItems() != null ? getItems() : new ArrayList<EventSubscriber>();
        ArrayList<SelectionTreeNodeModel> added = new ArrayList<>();
        ArrayList<EventSubscriber> removed = new ArrayList<>();

        // check what has been added:
        for (SelectionTreeNodeModel selectedEvent : selected) {
            boolean selectedInExisting = false;
            for (EventSubscriber existingEvent : existing) {
                if (selectedEvent.getTitle().equals(existingEvent.getEventUpName())) {
                    selectedInExisting = true;
                    break;
                }
            }

            if (!selectedInExisting) {
                added.add(selectedEvent);
            }
        }

        // check what has been deleted:
        for (EventSubscriber existingEvent : existing) {
            boolean existingInSelected = false;
            for (SelectionTreeNodeModel selectedEvent : selected) {
                if (selectedEvent.getTitle().equals(existingEvent.getEventUpName())) {
                    existingInSelected = true;
                    break;
                }
            }

            if (!existingInSelected) {
                removed.add(existingEvent);
            }
        }
        if (!StringHelper.isNullOrEmpty(model.getOldEmail())
                && !model.getOldEmail().equals(model.getEmail().getEntity())) {
            for (EventSubscriber a : existing) {
                toRemoveList.add(new EventSubscriptionParametesBase(new EventSubscriber(a.getEventUpName(),
                        EventNotificationMethod.SMTP,
                        a.getMethodAddress(),
                        a.getSubscriberId(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            for (SelectionTreeNodeModel a : selected) {
                toAddList.add(new EventSubscriptionParametesBase(new EventSubscriber(a.getTitle(),
                        EventNotificationMethod.SMTP,
                        model.getEmail().getEntity(),
                        getEntity().getId(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else {
            for (SelectionTreeNodeModel a : added) {
                toAddList.add(new EventSubscriptionParametesBase(new EventSubscriber(a.getTitle(),
                        EventNotificationMethod.SMTP,
                        model.getEmail().getEntity(),
                        getEntity().getId(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            for (EventSubscriber a : removed) {
                toRemoveList.add(new EventSubscriptionParametesBase(new EventSubscriber(a.getEventUpName(),
                        EventNotificationMethod.SMTP,
                        a.getMethodAddress(),
                        a.getSubscriberId(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (toRemoveList.size() > 0) {
            EventSubscriptionFrontendActionAsyncCallback callback = new EventSubscriptionFrontendActionAsyncCallback(toAddList, toRemoveList);
            for (ActionParametersBase param : toRemoveList) {
                Frontend.getInstance().runAction(ActionType.RemoveEventSubscription, param, callback);
            }
        } else if (toAddList.size() > 0) {
            Frontend.getInstance().runMultipleAction(ActionType.AddEventSubscription, toAddList);
        }
        cancel();
    }

    private static final class EventSubscriptionFrontendActionAsyncCallback implements IFrontendActionAsyncCallback {
        private ArrayList<ActionParametersBase> toAddList;
        ArrayList<ActionParametersBase> toRemoveList;
        private int sucessCount = 0;

        EventSubscriptionFrontendActionAsyncCallback(ArrayList<ActionParametersBase> toAddList, ArrayList<ActionParametersBase> toRemoveList) {
            this.toAddList = toAddList;
            this.toRemoveList = toRemoveList;
        }

        @Override
        public void executed(FrontendActionAsyncResult result) {
            ActionReturnValue returnValue = result.getReturnValue();
            if (returnValue != null && returnValue.getSucceeded()) {
                sucessCount++;
                // we wait until all subscribed events have been removed and then
                // invoke the AddEventSubscription action
                if (toAddList.size() > 0 && sucessCount == toRemoveList.size()) {
                    Frontend.getInstance().runMultipleAction(ActionType.AddEventSubscription, toAddList);
                }
            }
        }
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void itemsChanged() {
        super.itemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability() {
        if (getEntity() == null || getEntity().isGroup()) {
            getManageEventsCommand().setIsExecutionAllowed(false);
        } else {
            getManageEventsCommand().setIsExecutionAllowed(true);
        }
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getManageEventsCommand()) {
            manageEvents();
        }
        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "UserEventNotifierListModel"; //$NON-NLS-1$
    }
}
