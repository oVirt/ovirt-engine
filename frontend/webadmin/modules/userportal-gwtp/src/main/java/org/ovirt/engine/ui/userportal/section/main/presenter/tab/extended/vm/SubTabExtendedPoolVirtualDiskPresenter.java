package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalListModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedPoolVirtualDiskPresenter
        extends AbstractSubTabExtendedVmPresenter<PoolDiskListModel, SubTabExtendedPoolVirtualDiskPresenter.ViewDef, SubTabExtendedPoolVirtualDiskPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.extendedVirtualPoolDiskSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedPoolVirtualDiskPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<UserPortalItemModel> {
    }

    @TabInfo(container = ExtendedVmSubTabPanelPresenter.class)
    static TabData getTabData(ApplicationConstants applicationConstants,
            UserPortalSearchableDetailModelProvider<Disk, UserPortalListModel, PoolDiskListModel> modelProvider) {
        return new ModelBoundTabData(applicationConstants.extendedVirtualMachineVirtualDiskSubTabLabel(), 4, modelProvider);
    }

    @Inject
    public SubTabExtendedPoolVirtualDiskPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            UserPortalSearchableDetailModelProvider<Disk, UserPortalListModel, PoolDiskListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

}
