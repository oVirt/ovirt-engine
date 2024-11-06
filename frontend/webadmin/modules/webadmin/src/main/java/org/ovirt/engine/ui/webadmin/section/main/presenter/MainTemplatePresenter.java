package org.ovirt.engine.ui.webadmin.section.main.presenter;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.common.presenter.TemplateBreadCrumbsPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.TemplateActionPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagActivationChangeEvent;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagActivationChangeEvent.TagActivationChangeHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class MainTemplatePresenter extends AbstractMainWithDetailsPresenter<VmTemplate, TemplateListModel, MainTemplatePresenter.ViewDef, MainTemplatePresenter.ProxyDef> implements TagActivationChangeHandler {

    @GenEvent
    public class TemplateSelectionChange {

        List<VmTemplate> selectedItems;

    }

    private final TagEventCollector tagEventCollector;

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.templateMainPlace)
    public interface ProxyDef extends ProxyPlace<MainTemplatePresenter> {
    }

    public interface ViewDef extends AbstractMainWithDetailsPresenter.ViewDef<VmTemplate> {
        void setActiveTags(List<TagModel> tags);
    }

    @Inject
    public MainTemplatePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, MainModelProvider<VmTemplate, TemplateListModel> modelProvider,
            SearchPanelPresenterWidget<VmTemplate, TemplateListModel> searchPanelPresenterWidget,
            TemplateBreadCrumbsPresenterWidget breadCrumbs,
            TagEventCollector tagEventCollector,
            TemplateActionPanelPresenterWidget actionPanel) {
        super(eventBus, view, proxy, placeManager, modelProvider, searchPanelPresenterWidget, breadCrumbs, actionPanel);
        this.tagEventCollector = tagEventCollector;
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        TemplateSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getMainViewRequest() {
        return PlaceRequestFactory.get(WebAdminApplicationPlaces.templateMainPlace);
    }

    @Override
    protected void onBind() {
        super.onBind();
        registerHandler(getEventBus().addHandler(TagActivationChangeEvent.getType(), this));
        tagEventCollector.getActivationEvents().forEach(e -> onTagActivationChange(e));
        tagEventCollector.activateTemplates();
    }

    @Override
    public void onTagActivationChange(TagActivationChangeEvent event) {
        getView().setActiveTags(event.getActiveTags());
        setTags(event.getActiveTags());
    }

}
