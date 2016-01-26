package org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateDiskListModel;
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

public class SubTabExtendedTemplateVirtualDisksPresenter
        extends AbstractSubTabExtendedTemplatePresenter<UserPortalTemplateDiskListModel, SubTabExtendedTemplateVirtualDisksPresenter.ViewDef, SubTabExtendedTemplateVirtualDisksPresenter.ProxyDef> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @ProxyCodeSplit
    @NameToken(UserPortalApplicationPlaces.extendedTempplateVirtualDisksSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabExtendedTemplateVirtualDisksPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VmTemplate> {
    }

    @TabInfo(container = ExtendedTemplateSubTabPanelPresenter.class)
    static TabData getTabData() {
        return new TabDataBasic(constants.extendedTemplateVirtualDisksSubTabLabel(), 2);
    }

    @Inject
    public SubTabExtendedTemplateVirtualDisksPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, ExtendedTemplateMainTabSelectedItems selectedItems,
            UserPortalSearchableDetailModelProvider<DiskImage, UserPortalTemplateListModel,
                UserPortalTemplateDiskListModel> modelProvider) {
        super(eventBus, view, proxy, placeManager, selectedItems, modelProvider);
    }

}
