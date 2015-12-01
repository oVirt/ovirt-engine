package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostHooksListModel extends SearchableListModel<VDS, HashMap<String, String>> {

    public HostHooksListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().hostHooksTitle());
        setHelpTag(HelpTag.host_hooks);
        setHashName("host_hooks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            getSearchCommand().execute();
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

        if (!getEntity().isContainingHooks()) {
            setIsEmpty(true);
            setItems(new ArrayList<HashMap<String, String>>());
            return;
        }

        super.syncSearch();

        setIsEmpty(false);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue) {
                ArrayList<HashMap<String, String>> list = new ArrayList<>();
                HashMap<String, HashMap<String, HashMap<String, String>>> dictionary = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                HashMap<String, String> row;
                for (Map.Entry<String, HashMap<String, HashMap<String, String>>> keyValuePair : dictionary.entrySet()) {
                    for (Map.Entry<String, HashMap<String, String>> keyValuePair1 : keyValuePair.getValue()
                            .entrySet()) {
                        for (Map.Entry<String, String> keyValuePair2 : keyValuePair1.getValue().entrySet()) {
                            row = new HashMap<>();
                            row.put("EventName", keyValuePair.getKey()); //$NON-NLS-1$
                            row.put("ScriptName", keyValuePair1.getKey()); //$NON-NLS-1$
                            row.put("PropertyName", keyValuePair2.getKey()); //$NON-NLS-1$
                            row.put("PropertyValue", keyValuePair2.getValue()); //$NON-NLS-1$
                            list.add(row);
                        }
                    }
                }
                setItems(list);
            }
        };
        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsHooksById, tempVar, _asyncQuery);

    }

    @Override
    protected String getListName() {
        return "HostHooksListModel"; //$NON-NLS-1$
    }
}
