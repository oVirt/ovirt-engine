package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalSearchableDetailModelProvider;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabExtendedTemplateEventsPresenter
        extends AbstractSubTabExtendedTemplatePresenter<UserPortalTemplateEventListModel, SubTabExtendedTemplateEventsPresenter.ViewDef, SubTabExtendedTemplateEventsPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedTempplateEventsSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedTemplateEventsPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = ExtendedTemplateSubTabPanelPresenter.class)
    static TabData getTabData() {
        return new TabDataBasic(constants.extendedTemplateEventsSubTabLabel(), 3);
    }

    @Inject
    public SubTabExtendedTemplateEventsPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ExtendedTemplateMainTabSelectedItems selectedItems,
            UserPortalSearchableDetailModelProvider<AuditLog, UserPortalTemplateListModel,
                UserPortalTemplateEventListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, selectedItems, modelProvider);
    }

}
