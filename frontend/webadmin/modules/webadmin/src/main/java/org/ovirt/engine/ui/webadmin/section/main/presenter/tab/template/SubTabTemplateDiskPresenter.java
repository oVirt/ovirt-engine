package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.tab.ModelBoundTabData;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjector;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateSelectionChangeEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabTemplateDiskPresenter extends AbstractSubTabPresenter<VmTemplate, TemplateListModel, TemplateDiskListModel, SubTabTemplateDiskPresenter.ViewDef, SubTabTemplateDiskPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(ApplicationPlaces.templateDiskSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabTemplateDiskPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = TemplateSubTabPanelPresenter.class)
    static TabData getTabData(ClientGinjector ginjector) {
        return new ModelBoundTabData(ginjector.getApplicationConstants().templateDiskSubTabLabel(), 3,
                ginjector.getSubTabTemplateDiskModelProvider());
    }

    @Inject
    public SubTabTemplateDiskPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,
            SearchableDetailModelProvider<DiskImage, TemplateListModel, TemplateDiskListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, TemplateSubTabPanelPresenter.TYPE_SetTabContent, this);
    }

    @Override
    protected PlaceRequest getMainTabRequest() {
        return new PlaceRequest(ApplicationPlaces.templateMainTabPlace);
    }

    @ProxyEvent
    public void onTemplateSelectionChange(TemplateSelectionChangeEvent event) {
        updateMainTabSelection(event.getSelectedItems());
    }

}
