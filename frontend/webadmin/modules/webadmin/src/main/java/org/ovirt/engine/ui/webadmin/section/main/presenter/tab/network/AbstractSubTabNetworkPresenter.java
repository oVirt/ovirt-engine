package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public abstract class AbstractSubTabNetworkPresenter<D extends HasEntity<?>,
    V extends AbstractSubTabPresenter.ViewDef<NetworkView>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<NetworkView, NetworkListModel, D, V, P> {

    public AbstractSubTabNetworkPresenter(EventBus eventBus, V view, P proxy, PlaceManager placeManager,
            DetailModelProvider<NetworkListModel, D> modelProvider, NetworkMainSelectedItems selectedItems,
            DetailActionPanelPresenterWidget<?, ?, NetworkListModel, ?> actionPanel,
            NestedSlot slot) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel, slot);
    }

    @Override
    protected PlaceRequest getMainContentRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.networkMainPlace);
    }

    @Override
    protected List<NetworkView> filterByAdditionalParams(List<NetworkView> namedItems, PlaceRequest request) {
        Set<FragmentParams> params = FragmentParams.getParams(request);
        final String fragmentNameValue = request.getParameter(FragmentParams.DATACENTER.getName(), "");
        if (params.contains(FragmentParams.DATACENTER) && !"".equals(fragmentNameValue)) {
            return namedItems.stream().filter(item -> fragmentNameValue.equals(item.getDataCenterName()))
                    .collect(Collectors.toList());
        } else {
            return namedItems;
        }
    }

    @Override
    protected Map<String, String> getFragmentParamsFromEntity(NetworkView item) {
        Map<String, String> result = super.getFragmentParamsFromEntity(item);
        result.put(FragmentParams.DATACENTER.getName(), item.getDataCenterName());
        return result;
    }
}
