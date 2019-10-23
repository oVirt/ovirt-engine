package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.profile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public abstract class AbstractSubTabVnicProfilePresenter<D extends HasEntity<?>,
    V extends AbstractSubTabPresenter.ViewDef<VnicProfileView>, P extends TabContentProxyPlace<?>>
        extends AbstractSubTabPresenter<VnicProfileView, VnicProfileListModel, D, V, P> {

    public AbstractSubTabVnicProfilePresenter(EventBus eventBus, V view, P proxy, PlaceManager placeManager,
            DetailModelProvider<VnicProfileListModel, D> modelProvider, VnicProfileMainSelectedItems selectedItems,
            DetailActionPanelPresenterWidget<?, ?, VnicProfileListModel, D> actionPanel,
            NestedSlot slot) {
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, actionPanel, slot);
    }

    @Override
    protected PlaceRequest getMainContentRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.vnicProfileMainPlace);
    }

    @Override
    protected List<VnicProfileView> filterByAdditionalParams(List<VnicProfileView> namedItems, PlaceRequest request) {
        Set<FragmentParams> params = FragmentParams.getParams(request);
        final String dataCenterFragmentNameValue = request.getParameter(FragmentParams.DATACENTER.getName(), "");
        final String networkFragmentNameValue = request.getParameter(FragmentParams.NETWORK.getName(), "");
        if (params.contains(FragmentParams.DATACENTER) && !"".equals(dataCenterFragmentNameValue) &&
                params.contains(FragmentParams.NETWORK) && !"".equals(networkFragmentNameValue)) {
            return namedItems.stream().filter(item -> dataCenterFragmentNameValue.equals(item.getDataCenterName()) &&
                    networkFragmentNameValue.equals(item.getNetworkName()))
                    .collect(Collectors.toList());
        } else {
            return namedItems;
        }
    }

    @Override
    protected Map<String, String> getFragmentParamsFromEntity(VnicProfileView item) {
        Map<String, String> result = super.getFragmentParamsFromEntity(item);
        result.put(FragmentParams.DATACENTER.getName(), item.getDataCenterName());
        result.put(FragmentParams.NETWORK.getName(), item.getNetworkName());
        return result;
    }
}
