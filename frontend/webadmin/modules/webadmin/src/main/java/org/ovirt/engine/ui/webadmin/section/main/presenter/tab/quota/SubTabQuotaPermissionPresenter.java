package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota;

import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailPermissionActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabQuotaPermissionPresenter
    extends AbstractSubTabQuotaPresenter<QuotaPermissionListModel, SubTabQuotaPermissionPresenter.ViewDef,
        SubTabQuotaPermissionPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.quotaPermissionSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabQuotaPermissionPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<Quota> {
    }

    @TabInfo(container = QuotaSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.QUOTA_PERMISSION;
    }

    @Inject
    public SubTabQuotaPermissionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, QuotaMainSelectedItems selectedItems,
            DetailPermissionActionPanelPresenterWidget<Quota, QuotaListModel, QuotaPermissionListModel> actionPanel,
            SearchableDetailModelProvider<Permission, QuotaListModel, QuotaPermissionListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                QuotaSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
