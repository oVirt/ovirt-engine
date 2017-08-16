package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.uicommonweb.models.HostErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

/**
 * Presenter for the sub-sub tab (Hosts > General > Errata) that contains errata (singular: Erratum)
 * for the selected Host.
 */
public class SubTabHostErrataPresenter
    extends AbstractSubTabHostPresenter<HostErrataCountModel,
        SubTabHostErrataPresenter.ViewDef, SubTabHostErrataPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.hostGeneralErrataSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabHostErrataPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VDS> {
        AbstractUiCommandButton getTotalSecurity();
        AbstractUiCommandButton getTotalBugFix();
        AbstractUiCommandButton getTotalEnhancement();
        void showErrorMessage(SafeHtml errorMessage);
        void showCounts(ErrataCounts counts);
        void showProgress();
    }

    @TabInfo(container = HostSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.HOSTS_ERRATA;
    }

    private final HostErrataCountModel errataCountModel;

    @Inject
    public SubTabHostErrataPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager,  HostMainSelectedItems selectedItems,
            DetailTabModelProvider<HostListModel<Void>, HostErrataCountModel> modelProvider) {
        // No action panel on errata view, can pass null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                HostSubTabPanelPresenter.TYPE_SetTabContent);

        errataCountModel = getModelProvider().getModel();
    }

    @Override
    public void itemChanged(VDS item) {
        super.itemChanged(item);
        if (isVisible()) {
            updateModel();
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        updateModel();
    }

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        registerHandler(getView().getTotalSecurity().addClickHandler(event -> getView().getTotalSecurity().getCommand().execute()));

        registerHandler(getView().getTotalBugFix().addClickHandler(event -> getView().getTotalBugFix().getCommand().execute()));

        registerHandler(getView().getTotalEnhancement().addClickHandler(event -> getView().getTotalEnhancement().getCommand().execute()));


        // Handle the counts changing -> simple view update.
        //
        errataCountModel.addErrataCountsChangeListener((ev, sender, args) -> {
            // bus published message that the counts changed. update view.
            ErrataCounts counts = errataCountModel.getErrataCounts();
            getView().showCounts(counts);
        });

        // Handle the count model getting a query error -> simple view update.
        //
        errataCountModel.addPropertyChangeListener((ev, sender, args) -> {
            if ("Message".equals(args.propertyName)) { //$NON-NLS-1$
                if (errataCountModel.getMessage() != null && !errataCountModel.getMessage().isEmpty()) {
                    // bus published message that an error occurred communicating with Katello. Show the alert panel.
                    getView().showErrorMessage(SafeHtmlUtils.fromString(errataCountModel.getMessage()));
                }
            } else if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                if (errataCountModel.getProgress() != null) {
                    getView().showProgress();
                }
            }
        });
    }

    @Override
    protected void onBind() {
        super.onBind();

        getView().getTotalSecurity().setCommand(errataCountModel.getShowSecurityCommand());
        getView().getTotalBugFix().setCommand(errataCountModel.getShowBugsCommand());
        getView().getTotalEnhancement().setCommand(errataCountModel.getShowEnhancementsCommand());
    }


    private void updateModel() {
        VDS currentSelectedHost = getSelectedMainItems().getSelectedItem();
        if (currentSelectedHost != null) {
            errataCountModel.setGuid(currentSelectedHost.getId());
            errataCountModel.setEntity(currentSelectedHost);
            errataCountModel.runQuery(currentSelectedHost.getId());
        }
    }

}
