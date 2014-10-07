package org.ovirt.engine.ui.uicommonweb.models.datacenters.qos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.action.QosParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.profiles.ProfileBase;
import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public abstract class RemoveQosModel<T extends QosBase> extends ConfirmationModel {

    private final ListModel<T> sourceListModel;

    public RemoveQosModel(ListModel<T> sourceListModel) {
        this.sourceListModel = sourceListModel;

        setTitle(getTitle());
        setMessage();
        addCommands();
    }

    private void addCommands() {
        getCommands().add(new UICommand("onRemove", this).setTitle(ConstantsManager.getInstance().getConstants().ok()) //$NON-NLS-1$
                .setIsDefault(true));

        getCommands().add(new UICommand("cancel", this).setTitle(ConstantsManager.getInstance().getConstants().cancel()) //$NON-NLS-1$
                .setIsCancel(true));
    }

    @Override
    public abstract String getTitle();

    protected abstract VdcQueryType getProfilesByQosIdQueryType();

    protected abstract String getRemoveQosMessage(int size);

    protected abstract String getRemoveQosHashName();

    protected abstract HelpTag getRemoveQosHelpTag();

    protected abstract VdcActionType getRemoveActionType();

    private void setMessage() {
        ArrayList<VdcQueryParametersBase> parameters = new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queryTypes = new ArrayList<VdcQueryType>();
        for (T qos : sourceListModel.getSelectedItems()) {
            VdcQueryParametersBase parameter = new IdQueryParameters(qos.getId());
            parameters.add(parameter);
            queryTypes.add(getProfilesByQosIdQueryType());
        }
        Frontend.getInstance().runMultipleQueries(queryTypes, parameters, new IFrontendMultipleQueryAsyncCallback() {

            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {
                Map<ProfileBase, String> profilesAndQos = new HashMap<ProfileBase, String>();

                setHelpTag(getRemoveQosHelpTag());
                setHashName(getRemoveQosHashName());

                int index = 0;
                for (VdcQueryReturnValue returnValue : result.getReturnValues()) {
                        for (ProfileBase profileBase : (List<ProfileBase>)returnValue.getReturnValue()) {
                                profilesAndQos.put(profileBase, sourceListModel.getSelectedItems().get(index).getName());
                                        }
                        index++;
                }
                if (profilesAndQos.isEmpty()) {
                    ArrayList<String> list = new ArrayList<String>();
                    for (T item : sourceListModel.getSelectedItems()) {
                        list.add(item.getName());
                    }
                    setItems(list);
                } else {
                    setMessage(getRemoveQosMessage(profilesAndQos.size()));

                    ArrayList<String> list = new ArrayList<String>();
                    for (Entry<ProfileBase, String> item : profilesAndQos.entrySet()) {
                        list.add(item.getKey().getName() + " (" + item.getValue() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    setItems(list);
                }
            }
        });
    }

    public void onRemove() {
        ArrayList<VdcActionParametersBase> parameters = new ArrayList<VdcActionParametersBase>();

        for (T qos : sourceListModel.getSelectedItems()) {
            QosParametersBase<T> parameter = new QosParametersBase<T>();
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
