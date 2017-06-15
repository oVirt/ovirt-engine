package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ExternalSubnetParameters;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class RemoveExternalSubnetModel extends ConfirmationModel {

    private final List<ExternalSubnet> subnets;
    private final SearchableListModel<?, ?> sourceListModel;

    public RemoveExternalSubnetModel(SearchableListModel<?, ?> sourceListModel, List<ExternalSubnet> subnets) {
        setTitle(ConstantsManager.getInstance().getConstants().removeExternalSubnetTitle());
        setHelpTag(HelpTag.remove_external_subnet);
        setHashName("remove_external_subnet"); //$NON-NLS-1$

        this.sourceListModel = sourceListModel;
        this.subnets = subnets;

        ArrayList<String> items = new ArrayList<>();
        for (ExternalSubnet subnet : subnets) {
            items.add(subnet.getName());
        }
        setItems(items);

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnRemove", this); //$NON-NLS-1$
        getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
    }

    private void onRemove() {
        ArrayList<ActionParametersBase> list = new ArrayList<>();
        for (ExternalSubnet subnet : getSubnets()) {
            ActionParametersBase parameters = new ExternalSubnetParameters(subnet);
            list.add(parameters);

        }

        startProgress();

        Frontend.getInstance().runMultipleAction(ActionType.RemoveSubnetFromProvider,
                list,
                false,
                result -> {
                    stopProgress();
                    sourceListModel.getSearchCommand().execute();
                    cancel();
                },
                null);
    }

    private List<ExternalSubnet> getSubnets() {
        return subnets;
    }

    private void cancel() {
        sourceListModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("OnRemove".equals(command.getName())) { //$NON-NLS-1$
            onRemove();
        }
    }

}
