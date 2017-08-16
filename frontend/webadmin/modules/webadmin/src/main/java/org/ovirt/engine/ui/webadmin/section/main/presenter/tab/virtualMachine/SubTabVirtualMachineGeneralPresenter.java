package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.DetailTabDataIndex;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.TabData;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class SubTabVirtualMachineGeneralPresenter
    extends AbstractSubTabVirtualMachinePresenter<VmGeneralModel, SubTabVirtualMachineGeneralPresenter.ViewDef,
        SubTabVirtualMachineGeneralPresenter.ProxyDef> {

    @ProxyCodeSplit
    @NameToken(WebAdminApplicationPlaces.virtualMachineGeneralSubTabPlace)
    public interface ProxyDef extends TabContentProxyPlace<SubTabVirtualMachineGeneralPresenter> {
    }

    public interface ViewDef extends AbstractSubTabPresenter.ViewDef<VM> {
        /**
         * Clear all the alerts currently displayed in the alerts panel of the vm.
         */
        void clearAlerts();

        /**
         * Displays a new alert in the alerts panel of the vm.
         *
         * @param widget
         *            the widget used to display the alert, usually just a text label, but can also be a text label with
         *            a link to an action embedded
         */
        void addAlert(Widget widget);
    }

    @TabInfo(container = VirtualMachineSubTabPanelPresenter.class)
    static TabData getTabData() {
        return DetailTabDataIndex.VIRTUALMACHINE_GENERAL;
    }

    @Inject
    public SubTabVirtualMachineGeneralPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, VirtualMachineMainSelectedItems selectedItems,
            DetailModelProvider<VmListModel<Void>, VmGeneralModel> modelProvider) {
        // View has no actionPanel, passing null.
        super(eventBus, view, proxy, placeManager, modelProvider, selectedItems, null,
                VirtualMachineSubTabPanelPresenter.TYPE_SetTabContent);
    }

    @Override
    public void initializeHandlers() {
        super.initializeHandlers();

        // Initialize the list of alerts:
        final VmGeneralModel model = getModelProvider().getModel();
        updateAlerts(getView(), model);

        // Listen for changes in the properties of the model in order
        // to update the alerts panel:
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args.propertyName.contains("Alert")) { //$NON-NLS-1$
                updateAlerts(getView(), model);
            }
        });
    }

    /**
     * Review the model and if there are alerts add them to the view.
     *
     * @param view
     *            the view where alerts should be added
     * @param model
     *            the model to review
     */
    private void updateAlerts(final ViewDef view, final VmGeneralModel model) {
        // Clear all the alerts:
        view.clearAlerts();

        // Review the alerts and add those that are active:
        if (model.getHasAlert()) {
            addTextAlert(view, model.getAlert());
        }

    }

    /**
     * Create a widget containing text and add it to the alerts panel of the vm.
     *
     * @param view
     *            the view where the alert should be added
     * @param text
     *            the text content of the alert
     */
    private void addTextAlert(final ViewDef view, final String text) {
        final Label label = new Label(text);
        view.addAlert(label);
    }

}
