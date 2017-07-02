package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaUserListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaVmListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.inject.Inject;

public class DataCenterQuotaListModel extends QuotaListModel<StoragePool> {
    @Inject
    public DataCenterQuotaListModel(final QuotaClusterListModel quotaClusterListModel,
            final QuotaStorageListModel quotaStorageListModel, final QuotaVmListModel quotaVmListModel,
            final QuotaTemplateListModel quotaTemplateListModel, final QuotaUserListModel quotaUserListModel,
            final QuotaPermissionListModel quotaPermissionListModel, final QuotaEventListModel quotaEventListModel) {
        super(quotaClusterListModel, quotaStorageListModel, quotaVmListModel, quotaTemplateListModel,
                quotaUserListModel, quotaPermissionListModel, quotaEventListModel);
        setTitle(ConstantsManager.getInstance().getConstants().quotaTitle());
        setHelpTag(HelpTag.quota);
        setHashName("quota"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        IdQueryParameters parameters = new IdQueryParameters(getEntity().getId());
        parameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(QueryType.GetQuotaByStoragePoolId,
                parameters,
                new SetItemsAsyncQuery());
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("name")) { //$NON-NLS-1$
            getSearchCommand().execute();
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterQuotaListModel"; //$NON-NLS-1$
    }

    @Override
    protected void createQuota() {
        super.createQuota(false);

        QuotaModel quotaModel = (QuotaModel) getWindow();

        quotaModel.getDataCenter().setItems(Arrays.asList(getEntity()));
        quotaModel.getDataCenter().setSelectedItem(getEntity());
        quotaModel.getDataCenter().setIsChangeable(false);
    }
}
