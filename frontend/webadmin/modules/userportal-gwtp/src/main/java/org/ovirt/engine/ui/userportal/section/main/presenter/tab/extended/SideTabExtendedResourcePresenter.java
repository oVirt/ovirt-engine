package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.resources.ResourcesModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractModelActivationPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDataBoundModelProvider.DataChangeListener;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SideTabExtendedResourcePresenter extends AbstractModelActivationPresenter<VM, ResourcesModel,
    SideTabExtendedResourcePresenter.ViewDef, SideTabExtendedResourcePresenter.ProxyDef> implements DataChangeListener<VM> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private final UserPortalDataBoundModelProvider<VM, ResourcesModel> modelProvider;

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedResourceSideTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SideTabExtendedResourcePresenter> {
    }

    public interface ViewDef extends View, HasEditorDriver<ResourcesModel> {
    }

    @TabInfo(container = MainTabExtendedPresenter.class)
    static TabData getTabData() {
        return new TabDataBasic(constants.extendedResourceSideTabLabel(), 2);
    }

    @Inject
    public SideTabExtendedResourcePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            UserPortalDataBoundModelProvider<VM, ResourcesModel> modelProvider) {
        super(eventBus, view, proxy, modelProvider, MainTabExtendedPresenter.TYPE_SetTabContent);
        this.modelProvider = modelProvider;
        modelProvider.setDataChangeListener(this);
    }

    @Override
    public void onDataChange(List<VM> items) {
        getView().edit(modelProvider.getModel());
    }

}
