package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.Arrays;

import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Icon;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.FcpStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.ImportFcpStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.ImportIscsiStorageView;
import org.ovirt.engine.ui.common.widget.uicommon.storage.IscsiStorageView;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class StoragePopupView extends AbstractModelBoundPopupView<StorageModel>
        implements StoragePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<StorageModel, StoragePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, StoragePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<StoragePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField
    @Path(value = "comment.entity")
    @WithElementId("comment")
    StringEntityModelTextBoxEditor commentEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<StoragePool> datacenterListEditor;

    @UiField(provided = true)
    @Path(value = "availableStorageDomainTypeItems.selectedItem")
    @WithElementId("availableStorageDomainTypeItems")
    ListModelListBoxEditor<StorageDomainType> domainFunctionListEditor;

    @UiField(provided = true)
    @Path(value = "availableStorageTypeItems.selectedItem")
    @WithElementId("availableStorageTypeItems")
    ListModelListBoxEditor<StorageType> storageTypeListEditor;

    @UiField(provided = true)
    @Path(value = "format.selectedItem")
    @WithElementId("format")
    ListModelListBoxEditor<StorageFormatType> formatListEditor;

    @UiField(provided = true)
    @Path(value = "host.selectedItem")
    @WithElementId("host")
    ListModelListBoxEditor<VDS> hostListEditor;

    @UiField(provided = true)
    @Path(value = "activateDomain.entity")
    @WithElementId("activateDomainEditor")
    EntityModelCheckBoxEditor activateDomainEditor;

    @UiField
    @Path(value = "warningLowSpaceIndicator.entity")
    @WithElementId("warningLowSpaceIndicatorEditor")
    IntegerEntityModelTextBoxEditor warningLowSpaceIndicatorEditor;

    @UiField
    @Path(value = "warningLowConfirmedSpaceIndicator.entity")
    @WithElementId("warningLowConfirmedSpaceIndicatorEditor")
    IntegerEntityModelTextBoxEditor warningLowConfirmedSpaceIndicatorEditor;

    @UiField
    @Path(value = "warningLowSpaceSize.entity")
    @WithElementId("warningLowSpaceSizeLabel")
    Label warningLowSpaceSizeLabel;

    @UiField
    @Path(value = "criticalSpaceActionBlocker.entity")
    @WithElementId("criticalSpaceActionBlockerEditor")
    IntegerEntityModelTextBoxEditor criticalSpaceActionBlockerEditor;

    @Ignore
    @UiField
    FlowPanel specificStorageTypePanel;

    @UiField
    @Ignore
    AdvancedParametersExpander advancedParametersExpander;

    @UiField
    @Ignore
    FlowPanel advancedParametersExpanderContent;

    @UiField (provided = true)
    @Path(value = "wipeAfterDelete.entity")
    @WithElementId("wipeAfterDelete")
    EntityModelCheckBoxEditor wipeAfterDeleteEditor;

    @UiField (provided = true)
    @Path(value = "backup.entity")
    @WithElementId("backup")
    EntityModelCheckBoxEditor backup;

    @UiField (provided = true)
    @Path(value = "discardAfterDelete.entity")
    @WithElementId("discardAfterDelete")
    EntityModelCheckBoxEditor discardAfterDeleteEditor;

    @UiField (provided = true)
    @Ignore
    InfoIcon hostInfoIcon;

    @UiField
    FormGroup hostLabelIconListViewFormGroup;

    @UiField
    Icon datacenterAlertIcon;

    @SuppressWarnings("rawtypes")
    @Ignore
    @WithElementId
    AbstractStorageView storageView;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public StoragePopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initAdvancedParametersExpander();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void initListBoxEditors() {
        datacenterListEditor = new ListModelListBoxEditor<>(new AbstractRenderer<StoragePool>() {
            @Override
            public String render(StoragePool storage) {
                String formattedString = ""; //$NON-NLS-1$

                if (storage != null) {

                    // Get formatted storage type and format using Enum renders
                    String storageType = storage.isLocal() ? constants.storageTypeLocal() : ""; //$NON-NLS-1$
                    String storageFormatType = storage.getStoragePoolFormatType() == null ? "" : //$NON-NLS-1$
                            new EnumRenderer<StorageFormatType>().render(storage.getStoragePoolFormatType());

                    // Add storage type and format if available
                    if (!storageType.isEmpty() || !storageFormatType.isEmpty()) {
                        formattedString = " ("; //$NON-NLS-1$
                        if (storage.isLocal()) {
                            formattedString += storageType;
                        } else {
                            formattedString += storageFormatType;
                        }
                        formattedString += ")"; //$NON-NLS-1$
                    }

                    formattedString = storage.getName() + formattedString;
                }

                return formattedString;
            }
        });

        formatListEditor = new ListModelListBoxEditor<>(new EnumRenderer());

        hostListEditor = new ListModelListBoxEditor<>(new NameRenderer<VDS>());

        domainFunctionListEditor = new ListModelListBoxEditor<>(new EnumRenderer<StorageDomainType>());

        storageTypeListEditor = new ListModelListBoxEditor<>(new EnumRenderer<StorageType>());

        activateDomainEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        wipeAfterDeleteEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        backup = new EntityModelCheckBoxEditor(Align.RIGHT);
        discardAfterDeleteEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        hostInfoIcon = new InfoIcon(SafeHtmlUtils.fromString(constants.hostToUseToolTip()));
    }

    void localize() {
        criticalSpaceActionBlockerEditor.setLabelTooltip(constants.changeCriticalSpaceActionBlockerWarning());
    }

    @Override
    public void edit(StorageModel object) {
        driver.edit(object);

        final StorageModel storageModel = object;

        storageModel.getAvailableStorageDomainTypeItems().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            // Display the StorageTypes according to the selected StorageDomainFunction.
            updateStorageTypesByDomainType(storageModel);
        });

        storageModel.getAvailableStorageTypeItems().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            // Reveal the appropriate storage view according to the selected storage type
            revealStorageView(storageModel);
        });

        storageModel.getDataCenterAlert().getEntityChangedEvent().addListener((ev, sender, args) -> {
            datacenterAlertIcon.setVisible(storageModel.getDataCenterAlert().getIsAvailable());
            datacenterAlertIcon.setTitle(storageModel.getDataCenterAlert().getEntity());
        });

        warningLowSpaceIndicatorEditor.addKeyUpHandler(event -> {
            if (!storageModel.isNewStorage()) {
                storageModel.getWarningLowSpaceSize().setEntity(
                        ConstantsManager.getInstance().getMessages().bracketsWithGB(getWarningLowSpaceSize(storageModel)));
            }
        });

        criticalSpaceActionBlockerEditor.addKeyUpHandler(event -> {
            int storageDomainTotalSize = getStorageDomainTotalSize(storageModel);
            Integer criticalActionBlockerSpace = criticalSpaceActionBlockerEditor.asValueBox().getValue();
            if (criticalActionBlockerSpace == null || criticalActionBlockerSpace > storageDomainTotalSize) {
                criticalSpaceActionBlockerEditor.markAsInvalid(Arrays.asList(ConstantsManager.getInstance()
                        .getMessages()
                        .criticalSpaceActionBlockerBiggerThanStorageDomain(storageDomainTotalSize)));
            } else {
                criticalSpaceActionBlockerEditor.markAsValid();
            }
        });
    }

    private Integer getStorageDomainTotalSize(StorageModel storageModel) {
        return storageModel.getStorage() == null ? Integer.valueOf(0) : storageModel.getStorage().getTotalDiskSize();
    }

    private Integer getWarningLowSpaceSize(StorageModel storageModel) {
            Integer percentageValue = warningLowSpaceIndicatorEditor.asValueBox().getValue();
            return percentageValue == null ? 0 : storageModel.getStorage().getTotalDiskSize() * percentageValue / 100;
    }

    private void initAdvancedParametersExpander() {
        advancedParametersExpander.initWithContent(advancedParametersExpanderContent.getElement());
    }

    @SuppressWarnings("unchecked")
    private void revealStorageView(StorageModel object) {
        StorageType storageType = storageTypeListEditor.asEditor().getActualEditor().getValue();
        StorageDomainType storageDomainType = domainFunctionListEditor.asEditor().getActualEditor().getValue();

        if (storageType == null || storageDomainType == null) {
            return;
        }

        switch (storageType) {
            case NFS:
                storageView = new NfsStorageView();
                break;
            case LOCALFS:
                storageView = new LocalStorageView();
                break;
            case POSIXFS:
                storageView = new PosixStorageView();
                break;
            case MANAGED_BLOCK_STORAGE:
                storageView = new ManagedBlockStorageView();
                break;
            case GLUSTERFS:
                storageView = new GlusterStorageView();
                break;
            case FCP:
                if (!object.getBehavior().isImport()) {
                    storageView = new FcpStorageView(true);
                } else {
                    storageView = new ImportFcpStorageView();
                }
                break;
            case ISCSI:
                if (!object.getBehavior().isImport()) {
                    storageView = new IscsiStorageView(true);
                } else {
                    storageView = new ImportIscsiStorageView();
                }
                break;
            default:
                break;
        }

        updateStorageItem(object);
        IStorageModel model = object.getCurrentStorageItem();

        // Re-apply element IDs on 'storageView' change
        ViewIdHandler.idHandler.generateAndSetIds(this);

        // Clear the current storage view
        specificStorageTypePanel.clear();

        // Add the new storage view and call focus on it if needed
        if (storageView != null && model != null) {
            storageView.edit(model);
            specificStorageTypePanel.add(storageView);

            if (!nameEditor.isVisible()) {
                storageView.focus();
            }
        }
    }

    private void updateStorageItem(StorageModel storageModel) {
        storageModel.updateCurrentStorageItem();
    }

    private void updateStorageTypesByDomainType(StorageModel storageModel) {
        storageModel.getBehavior().setStorageTypeItems();
    }

    @Override
    public StorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);

        if (storageView != null) {
            if (!nameEditor.isVisible()) {
                storageView.focus();
            }
        }
    }

    @Override
    public boolean handleEnterKeyDisabled() {
        return storageView.isSubViewFocused();
    }

    @Override
    public void focusDiscardAfterDelete() {
        advancedParametersExpander.toggleExpander(true);
        discardAfterDeleteEditor.setFocus(true);
    }
}
