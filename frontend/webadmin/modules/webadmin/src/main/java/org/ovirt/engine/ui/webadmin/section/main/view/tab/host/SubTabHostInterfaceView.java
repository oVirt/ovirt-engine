package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractDetailTabListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListView;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItem;
import org.ovirt.engine.ui.common.widget.listgroup.PatternflyListViewItemCreator;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostInterfacePresenter;
import org.ovirt.engine.ui.webadmin.widget.host.HostNetworkInterfaceBondedListViewItem;
import org.ovirt.engine.ui.webadmin.widget.host.HostNetworkInterfaceListViewItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;

public class SubTabHostInterfaceView extends AbstractDetailTabListView<VDS, HostListModel<Void>, HostInterfaceListModel>
        implements SubTabHostInterfacePresenter.ViewDef, PatternflyListViewItemCreator<HostInterfaceLineModel> {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    private VDS currentMainModel;

    private PatternflyListView<VDS, HostInterfaceLineModel, HostInterfaceListModel> hostInterfaceListView;
    private SimplePanel progressIndicator;

    @Inject
    public SubTabHostInterfaceView(SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel<Void>,
            HostInterfaceListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider);
        hostInterfaceListView = new PatternflyListView<>();

        hostInterfaceListView.setCreator(this);
        hostInterfaceListView.setModel(modelProvider.getModel());
        getContentPanel().add(hostInterfaceListView);
        initWidget(getContentPanel());
        initProgressIndicator();
    }

    private void initProgressIndicator() {
        progressIndicator = new SimplePanel();
        getContainer().insert(progressIndicator, 0);
        getDetailModel().getPropertyChangedEvent().addListener((ev, sender, args) ->
            progressIndicator.getElement().setInnerSafeHtml(getDetailModel().isNetworkOperationInProgress() ?
                templates.networkUpdatingSpinner(constants.networkUpdating()) : SafeHtmlUtils.fromTrustedString("")
            )
        );
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AbstractSubTabPresenter.TYPE_SetActionPanel) {
            getContainer().insert(content, 0);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public void setMainSelectedItem(VDS selectedItem) {
        currentMainModel = selectedItem;
    }

    @Override
    public PatternflyListViewItem<HostInterfaceLineModel> createListViewItem(HostInterfaceLineModel selectedItem) {
        if (!selectedItem.getIsBonded()) {
            return new HostNetworkInterfaceListViewItem(selectedItem.getInterfaces().get(0).getName(), selectedItem);
        } else {
            return new HostNetworkInterfaceBondedListViewItem(selectedItem);
        }
    }

    @Override
    public void expandAll() {
        hostInterfaceListView.expandAll();
    }

    @Override
    public void collapseAll() {
        hostInterfaceListView.collapseAll();
    }
}
