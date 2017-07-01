package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;

public abstract class RemoveQosModel<T extends QosBase> extends ConfirmationModel {

    protected final ListModel<T> sourceListModel;

    public RemoveQosModel(ListModel<T> sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(getTitle());
        setMessage();
        addCommands();
    }

    private void addCommands() {
        getCommands().add(UICommand.createDefaultOkUiCommand("onRemove", this)); //$NON-NLS-1$

        getCommands().add(UICommand.createCancelUiCommand("cancel", this)); //$NON-NLS-1$
    }

    @Override
    public abstract String getTitle();

    protected abstract QueryType getUsingEntitiesByQosIdQueryType();

    protected abstract String getRemoveQosMessage(int size);

    protected abstract String getRemoveQosHashName();

    protected abstract HelpTag getRemoveQosHelpTag();

    protected abstract ActionType getRemoveActionType();

    private void setMessage() {
        ArrayList<QueryParametersBase> parameters = new ArrayList<>();
        ArrayList<QueryType> queryTypes = new ArrayList<>();
        for (T qos : sourceListModel.getSelectedItems()) {
            QueryParametersBase parameter = new IdQueryParameters(qos.getId());
            parameters.add(parameter);
            queryTypes.add(getUsingEntitiesByQosIdQueryType());
        }
        Frontend.getInstance().runMultipleQueries(queryTypes, parameters, result -> handleSetMessageQueryResult(result));
    }

    protected void handleSetMessageQueryResult(FrontendMultipleQueryAsyncResult result) {
        Map<String, String> entitiesAndQos = new HashMap<>();

        setHelpTag(getRemoveQosHelpTag());
        setHashName(getRemoveQosHashName());

        int index = 0;
        for (QueryReturnValue returnValue : result.getReturnValues()) {
            for (Nameable entity : (List<Nameable>) returnValue.getReturnValue()) {
                entitiesAndQos.put(entity.getName(), sourceListModel.getSelectedItems()
                        .get(index)
                        .getName());
            }
            index++;
        }
        if (entitiesAndQos.isEmpty()) {
            ArrayList<String> list = new ArrayList<>();
            for (T item : sourceListModel.getSelectedItems()) {
                list.add(item.getName());
            }
            setItems(list);
        } else {
            setMessage(getRemoveQosMessage(entitiesAndQos.size()));

            ArrayList<String> list = new ArrayList<>();
            for (Entry<String, String> item : entitiesAndQos.entrySet()) {
                list.add(item.getKey() + " (" + item.getValue() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            setItems(list);
        }
    }

    public void onRemove() {
        ArrayList<ActionParametersBase> parameters = new ArrayList<>();

        for (T qos : sourceListModel.getSelectedItems()) {
            QosParametersBase<T> parameter = new QosParametersBase<>();
            parameter.setQosId(qos.getId());
            parameters.add(parameter);
        }

        Frontend.getInstance().runMultipleAction(getRemoveActionType(), parameters);

        cancel();
    }

    private void cancel() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
        if ("onRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        } else if ("cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }
}
