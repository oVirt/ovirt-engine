package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.RemoveVmTemplateInterfaceModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NetworkTemplateListModel extends SearchableListModel<NetworkView, PairQueryable<VmNetworkInterface, VmTemplate>> {
    private UICommand removeCommand;

    public UICommand getRemoveCommand() {
        return removeCommand;
    }

    private void setRemoveCommand(UICommand value) {
        removeCommand = value;
    }

    public NetworkTemplateListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHelpTag(HelpTag.templates);
        setHashName("templates"); //$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);

        setComparator(Comparator
                .comparing((PairQueryable<VmNetworkInterface, VmTemplate> p) -> p.getSecond().getClusterName())
                .thenComparing(p -> p.getSecond().getName()));

        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
    }

    @Override
    public void setEntity(NetworkView value) {
        if (value == null || !value.equals(getEntity())) {
            super.setEntity(value);
        }
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

        IdQueryParameters params = new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());

        Frontend.getInstance().runQuery(QueryType.GetVmTemplatesAndNetworkInterfacesByNetworkId, params, new SetItemsAsyncQuery());
    }

    private void updateActionAvailability() {
        getRemoveCommand().setIsExecutionAllowed(getSelectedItems() != null && !getSelectedItems().isEmpty());
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }

        List<VmNetworkInterface> vnics = new ArrayList<>();
        for (PairQueryable<VmNetworkInterface, VmTemplate> pair : getSelectedItems()) {
            vnics.add(pair.getFirst());
        }
        RemoveVmTemplateInterfaceModel model = new RemoveVmTemplateInterfaceModel(this, vnics, true);
        setWindow(model);

    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRemoveCommand()) {
            remove();
        }
    }

    @Override
    protected String getListName() {
        return "NetworkTemplateListModel"; //$NON-NLS-1$
    }
}
