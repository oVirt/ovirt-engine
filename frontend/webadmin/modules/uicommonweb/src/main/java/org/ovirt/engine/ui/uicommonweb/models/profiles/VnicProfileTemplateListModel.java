package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class VnicProfileTemplateListModel extends SearchableListModel
{

    public VnicProfileTemplateListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHashName("templates"); //$NON-NLS-1$

        updateActionAvailability();
    }

    @Override
    public VnicProfile getEntity() {
        return (VnicProfile) super.getEntity();
    }

    public void setEntity(VnicProfile value) {
        super.setEntity(value);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    public void setItems(Iterable value) {
        if (value != null) {
            List<VmTemplate> itemList = (List<VmTemplate>) value;
            Collections.sort(itemList, new Linq.VmTemplateComparator());
        }
        super.setItems(value);
    }

    @Override
    public void search() {
        if (getEntity() != null)
        {
            super.search();
        }
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null)
        {
            return;
        }

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                VnicProfileTemplateListModel.this.setItems((List<VmTemplate>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        IdQueryParameters params =
                new IdQueryParameters(getEntity().getId());
        params.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetTemplatesByVnicProfileId,
                params,
                asyncQuery);
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
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
        return "VnicProfileTemplateListModel"; //$NON-NLS-1$
    }
}
