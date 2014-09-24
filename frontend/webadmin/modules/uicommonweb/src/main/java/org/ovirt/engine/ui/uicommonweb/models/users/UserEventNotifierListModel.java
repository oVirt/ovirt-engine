package org.ovirt.engine.ui.uicommonweb.models.users;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationEntity;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.action.EventSubscriptionParametesBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
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
public class UserEventNotifierListModel extends SearchableListModel
{

    private UICommand privateManageEventsCommand;

    public UICommand getManageEventsCommand()
    {
        return privateManageEventsCommand;
    }

    private void setManageEventsCommand(UICommand value)
    {
        privateManageEventsCommand = value;
    }

    @Override
    public DbUser getEntity()
    {
        return (DbUser) super.getEntity();
    }

    public void setEntity(DbUser value)
    {
        super.setEntity(value);
    }

    public UserEventNotifierListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().eventNotifierTitle());
        setHelpTag(HelpTag.event_notifier);
        setHashName("event_notifier"); //$NON-NLS-1$

        setManageEventsCommand(new UICommand("ManageEvents", this)); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void syncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.syncSearch();

        super.syncSearch(VdcQueryType.GetEventSubscribersBySubscriberIdGrouped,
                new IdQueryParameters(getEntity().getId()));
    }

    public void manageEvents()
    {
        EventNotificationModel model = new EventNotificationModel();
        setWindow(model);

        model.setTitle(ConstantsManager.getInstance().getConstants().addEventNotificationTitle());
        model.setHelpTag(HelpTag.add_event_notification);
        model.setHashName("add_event_notification"); //$NON-NLS-1$

        ArrayList<EventNotificationEntity> eventTypes =
                ApplicationModeHelper.getModeSpecificEventNotificationTypeList();
        Map<EventNotificationEntity, HashSet<AuditLogType>> availableEvents =
                AsyncDataProvider.getInstance().getAvailableNotificationEvents();

        Translator translator = EnumTranslator.getInstance();

        ArrayList<SelectionTreeNodeModel> list = new ArrayList<SelectionTreeNodeModel>();

        ArrayList<event_subscriber> items =
                getItems() == null ? new ArrayList<event_subscriber>()
                        : Linq.<event_subscriber> cast(getItems());
        for (EventNotificationEntity eventType : eventTypes)
        {
            SelectionTreeNodeModel stnm = new SelectionTreeNodeModel();
            stnm.setTitle(eventType.toString());
            stnm.setDescription(translator.get(eventType));
            list.add(stnm);

            for (AuditLogType logtype : availableEvents.get(eventType))
            {
                SelectionTreeNodeModel eventGrp = new SelectionTreeNodeModel();

                String description;
                try {
                    description = translator.get(logtype);
                } catch (MissingResourceException e) {
                    description = logtype.toString();
                }

                eventGrp.setTitle(logtype.toString());
                eventGrp.setDescription(description);
                eventGrp.setParent(list.get(list.size() - 1));
                eventGrp.setIsSelectedNotificationPrevent(true);
                eventGrp.setIsSelectedNullable(false);
                for (event_subscriber es : items)
                {
                    if (es.getevent_up_name().equals(logtype.toString()))
                    {
                        eventGrp.setIsSelectedNullable(true);
                        break;
                    }
                }

                list.get(list.size() - 1).getChildren().add(eventGrp);
                eventGrp.setIsSelectedNotificationPrevent(false);
            }
            if (list.get(list.size() - 1).getChildren().size() > 0)
            {
                list.get(list.size() - 1).getChildren().get(0).updateParentSelection();
            }
        }

        model.setEventGroupModels(list);
        if (!StringHelper.isNullOrEmpty(getEntity().getEmail()))
        {
            model.getEmail().setEntity(getEntity().getEmail());
        }
        else if (items.size() > 0)
        {
            model.getEmail().setEntity(items.get(0).getmethod_address());
        }

        model.setOldEmail((String) model.getEmail().getEntity());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onSave()
    {
        EventNotificationModel model = (EventNotificationModel) getWindow();

        if (!model.validate())
        {
            return;
        }

        ArrayList<VdcActionParametersBase> toAddList = new ArrayList<VdcActionParametersBase>();
        ArrayList<VdcActionParametersBase> toRemoveList = new ArrayList<VdcActionParametersBase>();

        // var selected = model.EventGroupModels.SelectMany(a => a.Children).Where(a => a.IsSelected == true);
        ArrayList<SelectionTreeNodeModel> selected = new ArrayList<SelectionTreeNodeModel>();
        for (SelectionTreeNodeModel node : model.getEventGroupModels())
        {
            for (SelectionTreeNodeModel child : node.getChildren())
            {
                if (child.getIsSelectedNullable() != null && child.getIsSelectedNullable().equals(true))
                {
                    selected.add(child);
                }
            }
        }

        ArrayList<event_subscriber> existing =
                getItems() != null ? Linq.<event_subscriber> cast(getItems())
                        : new ArrayList<event_subscriber>();
        ArrayList<SelectionTreeNodeModel> added = new ArrayList<SelectionTreeNodeModel>();
        ArrayList<event_subscriber> removed = new ArrayList<event_subscriber>();

        // check what has been added:
        for (SelectionTreeNodeModel selectedEvent : selected)
        {
            boolean selectedInExisting = false;
            for (event_subscriber existingEvent : existing)
            {
                if (selectedEvent.getTitle().equals(existingEvent.getevent_up_name()))
                {
                    selectedInExisting = true;
                    break;
                }
            }

            if (!selectedInExisting)
            {
                added.add(selectedEvent);
            }
        }

        // check what has been deleted:
        for (event_subscriber existingEvent : existing)
        {
            boolean existingInSelected = false;
            for (SelectionTreeNodeModel selectedEvent : selected)
            {
                if (selectedEvent.getTitle().equals(existingEvent.getevent_up_name()))
                {
                    existingInSelected = true;
                    break;
                }
            }

            if (!existingInSelected)
            {
                removed.add(existingEvent);
            }
        }
        if (!StringHelper.isNullOrEmpty(model.getOldEmail())
                && !model.getOldEmail().equals(model.getEmail().getEntity()))
        {
            for (event_subscriber a : existing)
            {
                toRemoveList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getevent_up_name(),
                        EventNotificationMethod.SMTP,
                        a.getmethod_address(),
                        a.getsubscriber_id(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
            for (SelectionTreeNodeModel a : selected)
            {
                toAddList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getTitle(),
                        EventNotificationMethod.SMTP,
                        (String) model.getEmail().getEntity(),
                        getEntity().getId(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        else
        {
            for (SelectionTreeNodeModel a : added)
            {
                toAddList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getTitle(),
                        EventNotificationMethod.SMTP,
                        (String) model.getEmail().getEntity(),
                        getEntity().getId(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }

            for (event_subscriber a : removed)
            {
                toRemoveList.add(new EventSubscriptionParametesBase(new event_subscriber(a.getevent_up_name(),
                        EventNotificationMethod.SMTP,
                        a.getmethod_address(),
                        a.getsubscriber_id(), ""), "")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        if (toRemoveList.size() > 0)
        {
            EventSubscriptionFrontendActionAsyncCallback callback = new EventSubscriptionFrontendActionAsyncCallback(toAddList, toRemoveList);
            for (VdcActionParametersBase param : toRemoveList)
            {
                Frontend.getInstance().runAction(VdcActionType.RemoveEventSubscription, param, callback);
            }
        } else if (toAddList.size() > 0)
        {
            Frontend.getInstance().runMultipleAction(VdcActionType.AddEventSubscription, toAddList);
        }
        cancel();
    }

    private static final class EventSubscriptionFrontendActionAsyncCallback implements IFrontendActionAsyncCallback {
        private ArrayList<VdcActionParametersBase> toAddList;
        ArrayList<VdcActionParametersBase> toRemoveList;
        private int sucessCount = 0;

        EventSubscriptionFrontendActionAsyncCallback(ArrayList<VdcActionParametersBase> toAddList, ArrayList<VdcActionParametersBase> toRemoveList) {
            this.toAddList = toAddList;
            this.toRemoveList = toRemoveList;
        }

        @Override
        public void executed(FrontendActionAsyncResult result) {
            VdcReturnValueBase returnValue = result.getReturnValue();
            if (returnValue != null && returnValue.getSucceeded()) {
                sucessCount++;
                // we wait until all subscribed events have been removed and then
                // invoke the AddEventSubscription action
                if (toAddList.size() > 0 && sucessCount == toRemoveList.size()) {
                    Frontend.getInstance().runMultipleAction(VdcActionType.AddEventSubscription, toAddList);
                }
            }
        }
    }

    public void cancel()
    {
        setWindow(null);
    }

    @Override
    protected void itemsChanged()
    {
        super.itemsChanged();
        updateActionAvailability();
    }

    private void updateActionAvailability()
    {
        if (getEntity() == null || getEntity().isGroup() == true)
        {
            getManageEventsCommand().setIsExecutionAllowed(false);
        }
        else
        {
            getManageEventsCommand().setIsExecutionAllowed(true);
        }
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getManageEventsCommand())
        {
            manageEvents();
        }
        if ("OnSave".equals(command.getName())) //$NON-NLS-1$
        {
            onSave();
        }
        if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "UserEventNotifierListModel"; //$NON-NLS-1$
    }
}
