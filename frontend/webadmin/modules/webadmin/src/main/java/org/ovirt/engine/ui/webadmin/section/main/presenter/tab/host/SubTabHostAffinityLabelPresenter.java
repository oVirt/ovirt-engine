package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.labels.list.HostAffinityLabelListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.AffinityLabelsActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabHostAffinityLabelPresenter
        extends AbstractSubTabHostPresenter<HostAffinityLabelListModel,
        SubTabHostAffinityLabelPresenter.ViewDef, SubTabHostAffinityLabelPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostAffinityLabelsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostAffinityLabelPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.HOSTS_AFFINITY_LABELS;
    }

    @Inject
    public SubTabHostAffinityLabelPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, HostMainSelectedItems selectedItems,
            AffinityLabelsActionPanelPresenterWidget<VDS, HostListModel<Void>, HostAffinityLabelListModel> actionPanel,
            SearchableDetailModelProvider<Label, HostListModel<Void>, HostAffinityLabelListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel,
                HostSubTabPanelPresenter.TYPE_SetTabContent);
    }

}
