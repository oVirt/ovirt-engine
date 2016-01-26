package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.place.PlaceRequestFactory;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractSideTabWithDetailsPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class SideTabExtendedTemplatePresenter extends AbstractSideTabWithDetailsPresenter<VmTemplate, UserPortalTemplateListModel, SideTabExtendedTemplatePresenter.ViewDef, SideTabExtendedTemplatePresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @GenEvent
    public class ExtendedTemplateSelectionChange {

        List<VmTemplate> selectedItems;

    }

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedTemplateSideTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SideTabExtendedTemplatePresenter> {
    }

    public interface ViewDef extends AbstractSideTabWithDetailsPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = MainTabExtendedPresenter.class)
    static TabData getTabData() {
        return new TabDataBasic(constants.extendedTemplateSideTabLabel(), 1);
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetSubTabPanelContent = new Type<>();

    @Inject
    public SideTabExtendedTemplatePresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, UserPortalTemplateListProvider modelProvider) {
        super(eventBus, view, proxy, placeManager, modelProvider);
    }

    @Override
    protected void fireTableSelectionChangeEvent() {
        ExtendedTemplateSelectionChangeEvent.fire(this, getSelectedItems());
    }

    @Override
    protected PlaceRequest getSideTabRequest() {
        return PlaceRequestFactory.get(UserPortalApplicationPlaces.extendedTemplateSideTabPlace);
    }

}
