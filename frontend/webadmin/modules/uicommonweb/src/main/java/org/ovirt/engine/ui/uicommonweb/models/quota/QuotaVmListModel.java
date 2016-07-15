package org.ovirt.engine.ui.uicommonweb.models.quota;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class QuotaVmListModel extends SearchableListModel<Quota, VM> {

    public QuotaVmListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().vmsTitle());
        setHelpTag(HelpTag.vms);
        setHashName("vms"); //$NON-NLS-1$
        setIsTimerDisabled(true);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        super.syncSearch();

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetVmsRelatedToQuotaId, tempVar, new AsyncQuery<>(new AsyncCallback<VdcQueryReturnValue>() {
            @Override
            public void onSuccess(VdcQueryReturnValue returnValue) {
                setItems((ArrayList<VM>) returnValue.getReturnValue());
                setIsEmpty(((List) getItems()).size() == 0);
            }
        }));
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected String getListName() {
        return "QuotaVmListModel"; //$NON-NLS-1$
    }

}
