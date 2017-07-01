package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VnicProfileVmListModel extends SearchableListModel<VnicProfileView, VM> {

    public VnicProfileVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().virtualMachinesTitle());
        setHelpTag(HelpTag.virtual_machines);
        setHashName("virtual_machines"); //$NON-NLS-1$

        setComparator(new LexoNumericNameableComparator<>());

        updateActionAvailability();
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

        IdQueryParameters params =
                new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetVmsByVnicProfileId, params, new AsyncQuery<QueryReturnValue>(
                returnValue -> setItems((Collection<VM>) returnValue.getReturnValue())));
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    private void updateActionAvailability() {
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

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);
    }

    @Override
    protected String getListName() {
        return "VnicProfileVmListModel"; //$NON-NLS-1$
    }
}
