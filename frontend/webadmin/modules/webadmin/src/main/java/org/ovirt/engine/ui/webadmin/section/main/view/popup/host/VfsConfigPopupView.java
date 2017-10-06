package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.ArrayList;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable.SelectionMode;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.AbstractCheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractCheckboxHeader;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel.AllNetworksSelector;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigNetwork;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.VfsConfigPopupPresenterWidget;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.inject.Inject;

public class VfsConfigPopupView extends AbstractModelBoundPopupView<VfsConfigModel> implements VfsConfigPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<VfsConfigModel, VfsConfigPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VfsConfigPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<VfsConfigPopupView> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Ignore
    AdvancedParametersExpander numVfsExpander;

    @UiField
    @Ignore
    FlowPanel numVfsExpanderContent;

    @UiField
    @Path(value = "numOfVfs.entity")
    IntegerEntityModelTextBoxEditor numOfVfs;

    @UiField(provided = true)
    @Path(value = "maxNumOfVfs.entity")
    ValueLabel<Integer> maxVfsLabel;

    @UiField
    @Ignore
    Label allowedNetworksLabel;

    @UiField
    @Ignore
    Label selectNetworksLabel;

    @UiField
    @Path(value = "allNetworksAllowed.selectedItem")
    ListModelRadioGroupEditor<AllNetworksSelector> allNetworksSelectorEditor;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel<VfsConfigNetwork>> networks;

    @UiField
    FlowPanel allowedNetworksPanel;

    @UiField(provided = true)
    @Ignore
    VfsNicLabelWidget labelsWidget;

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    @Inject
    public VfsConfigPopupView(EventBus eventBus) {
        super(eventBus);
        maxVfsLabel = new ValueLabel<>(new AbstractRenderer<Integer>() {

            @Override
            public String render(Integer object) {
                return messages.maxVfs(object);
            }
        });

        networks = new EntityModelCellTable<>(SelectionMode.NONE, true);

        labelsWidget = new VfsNicLabelWidget();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);

        initExpander(constants);
        driver.initialize(this);
    }

    private void initExpander(ApplicationConstants constants) {
        numVfsExpander.initWithContent(numVfsExpanderContent.getElement());
        numVfsExpander.setTitleWhenExpanded(constants.numOfVfsSetting());
        numVfsExpander.setTitleWhenCollapsed(constants.numOfVfsSetting());
    }

    @Override
    public void edit(final VfsConfigModel model) {
        driver.edit(model);

        labelsWidget.edit(model.getLabelsModel());
        networks.asEditor().edit(model.getNetworks());
        initNetworksTable();

        labelsWidget.addValueChangeHandler(event -> refreshNetworksTable());

        updateAllowedNetworksPanelVisibility(model);
        model.getAllNetworksAllowed().getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateAllowedNetworksPanelVisibility(model));
    }

    private void updateAllowedNetworksPanelVisibility(final VfsConfigModel model) {
        boolean visible = AllNetworksSelector.specificNetworks == model.getAllNetworksAllowed().getSelectedItem();
        allowedNetworksPanel.setVisible(visible);
        if (visible) {
            refreshNetworksTable();
        }
    }

    @Override
    public VfsConfigModel flush() {
        labelsWidget.flush();
        networks.asEditor().flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    void initNetworksTable() {
        networks.enableColumnResizing();

        networks.addColumn(
                new AttachedIndicatorCheckboxColumn(),
                new AttachedIndicatorCheckboxHeader(), "20px"); //$NON-NLS-1$

        networks.addColumn(new AbstractTextColumn<VfsConfigNetwork>() {
            @Override
            public String getValue(VfsConfigNetwork object) {
                return object.getEntity().getName();
            }
        }, constants.vfsConfigNetworkName(), "120px"); //$NON-NLS-1$

        networks.addColumn(new AbstractTextColumn<VfsConfigNetwork>() {
            @Override
            public String getValue(VfsConfigNetwork object) {
                return object.getLabelViaAttached();
            }
        }, constants.vfsConfigViaLabel(), "120px"); //$NON-NLS-1$

    }

    private final class AttachedIndicatorCheckboxColumn extends AbstractCheckboxColumn<VfsConfigNetwork> {
        private AttachedIndicatorCheckboxColumn() {
            super(new AttachedIndicatorFieldUpdater());
        }

        @Override
        public Boolean getValue(VfsConfigNetwork vfsConfigNetwork) {
            return shouldNetworkBeDisplayedAsAttached(vfsConfigNetwork);
        }

        @Override
        protected boolean canEdit(VfsConfigNetwork vfsConfigNetwork) {
            return canEditAssign(vfsConfigNetwork);
        }
    }

    private final class AttachedIndicatorFieldUpdater implements FieldUpdater<VfsConfigNetwork, Boolean> {
        @Override
        public void update(int index, VfsConfigNetwork vfsConfigNetwork, Boolean value) {
            vfsConfigNetwork.setAttached(value);
            refreshNetworksTable();
        }
    }

    private final class AttachedIndicatorCheckboxHeader extends AbstractCheckboxHeader {

        @Override
        protected void selectionChanged(Boolean value) {
            for (VfsConfigNetwork vfsConfigNetwork : getNetworksTableItems()) {
                if (!vfsConfigNetwork.isAttachedViaLabel()) {
                    vfsConfigNetwork.setAttached(value);
                }
            }
            refreshNetworksTable();
        }

        @Override
        public Boolean getValue() {
            for (VfsConfigNetwork vfsConfigNetwork : getNetworksTableItems()) {
                if (!shouldNetworkBeDisplayedAsAttached(vfsConfigNetwork)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isEnabled() {
            for (VfsConfigNetwork vfsConfigNetwork : getNetworksTableItems()) {
                if (canEditAssign(vfsConfigNetwork)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public SafeHtml getTooltip() {
            return VfsConfigPopupView.templates.textForCheckBoxHeader(constants.attachAll());
        }
    }

    private boolean canEditAssign(VfsConfigNetwork vfsConfigNetwork) {
        return !vfsConfigNetwork.isAttachedViaLabel();
    }

    private boolean shouldNetworkBeDisplayedAsAttached(VfsConfigNetwork vfsConfigNetwork) {
        return vfsConfigNetwork.isAttached() || vfsConfigNetwork.isAttachedViaLabel();
    }

    private void refreshNetworksTable() {
        networks.asEditor().edit(networks.asEditor().flush());
    }

    private Iterable<VfsConfigNetwork> getNetworksTableItems() {
        ListModel<VfsConfigNetwork> tableModel = networks.asEditor().flush();
        return tableModel != null ? tableModel.getItems() : new ArrayList<VfsConfigNetwork>();
    }
}
