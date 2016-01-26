package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.users.UserEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabUserEventPresenter
    extends AbstractSubTabUserPresenter<UserEventListModel, SubTabUserEventPresenter.ViewDef,
        SubTabUserEventPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.userEventSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabUserEventPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<DbUser> {
    }

    @TabInfo(container = UserSubTabPanelPresenter.class)
    static TabData getTabData(
            SearchableDetailModelProvider<AuditLog, UserListModel, UserEventListModel> modelProvider) {
        return new ModelBoundTabData(constants.userEventSubTabLabel(), 5, modelProvider, Align.RIGHT);
    }

    @Inject
    public SubTabUserEventPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, UserMainTabSelectedItems selectedItems,
            SearchableDetailModelProvider<AuditLog, UserListModel, UserEventListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems,
                UserSubTabPanelPresenter.TYPE_SetTabContent);
    }
}
