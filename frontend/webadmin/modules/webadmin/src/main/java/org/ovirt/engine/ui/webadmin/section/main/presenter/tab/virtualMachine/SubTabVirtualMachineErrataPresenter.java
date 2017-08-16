package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.uicommonweb.models.VmErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
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
 * Presenter for the sub tab (VMs > Errata) that contains errata (singular: Erratum)
 * for the selected VM.
 */
public class SubTabVirtualMachineErrataPresenter
    extends AbstractSubTabVirtualMachinePresenter<VmErrataCountModel, SubTabVirtualMachineErrataPresenter.ViewDef,
        SubTabVirtualMachineErrataPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineErrataSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineErrataPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
        AbstractUiCommandButton getTotalSecurity();
        AbstractUiCommandButton getTotalBugFix();
        AbstractUiCommandButton getTotalEnhancement();
        void showErrorMessage(SafeHtml errorMessage);
        void clearErrorMessage();
        void showCounts(ErrataCounts counts);
        void showProgress();
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VIRTUALMACHINE_ERRATA;
    }

    private final VmErrataCountModel errataCountModel;

    @Inject
    public SubTabVirtualMachineErrataPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainSelectedItems selectedItems,
            DetailTabModelProvider<VmListModel<Void>, VmErrataCountModel> errataCountModelProvider) {
        // View has no action panel, passing null.
        super(eventBus, view, proxy, placeManager, errataCountModelProvider, selectedItems, null,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
        errataCountModel = errataCountModelProvider.getModel();
    }

    @Override
    public void itemChanged(VM item) {
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
    protected void onBind() {
        super.onBind();

        getView().getTotalSecurity().setCommand(errataCountModel.getShowSecurityCommand());
        getView().getTotalBugFix().setCommand(errataCountModel.getShowBugsCommand());
        getView().getTotalEnhancement().setCommand(errataCountModel.getShowEnhancementsCommand());

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
                // bus published message that an error occurred communicating with Katello. Show the alert panel.
                if (errataCountModel.getMessage() != null && !errataCountModel.getMessage().isEmpty()) {
                    getView().showErrorMessage(SafeHtmlUtils.fromString(errataCountModel.getMessage()));
                } else {
                    getView().clearErrorMessage();
                }
            } else if (PropertyChangedEventArgs.PROGRESS.equals(args.propertyName)) {
                if (errataCountModel.getProgress() != null) {
                    getView().showProgress();
                }
            }
        });
    }

    private void updateModel() {
        VM currentSelectedVm = getSelectedMainItems().getSelectedItem();
        if (currentSelectedVm != null) {
            // Update the model with data from the backend
            errataCountModel.setGuid(currentSelectedVm.getId());
            errataCountModel.setEntity(currentSelectedVm);
            errataCountModel.runQuery(currentSelectedVm.getId());
        }
    }

}
