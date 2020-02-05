package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.core.common.businessentities.OpenStackApiVersionType;
import org.ovirt.engine.core.common.businessentities.OpenStackProtocolType;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.KVMPropertiesWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.KubevirtPropertiesWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.VmwarePropertiesWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.XENPropertiesWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

public class ProviderPopupView extends AbstractModelBoundPopupView<ProviderModel> implements ProviderPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ProviderModel, ProviderPopupView> {}

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ProviderPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ProviderPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    StringEntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "type.selectedItem")
    @WithElementId
    ListModelListBoxEditor<ProviderType> typeEditor;

    @UiField(provided = true)
    @Path(value = "autoSync.entity")
    @WithElementId
    EntityModelCheckBoxEditor autoSyncEditor;

    @UiField(provided = true)
    @Path(value = "isUnmanaged.entity")
    @WithElementId
    EntityModelCheckBoxEditor isUnmanagedEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId
    ListModelListBoxEditor<StoragePool> datacenterEditor;

    @UiField
    @Path(value = "url.entity")
    @WithElementId
    StringEntityModelTextBoxEditor urlEditor;

    @UiField
    UiCommandButton testButton;

    @UiField
    @Ignore
    AlertPanel testResultMessage;

    @UiField(provided = true)
    @Path(value = "requiresAuthentication.entity")
    @WithElementId
    EntityModelCheckBoxEditor requiresAuthenticationEditor;

    @UiField(provided = true)
    @Path(value = "authApiVersion.selectedItem")
    @WithElementId
    ListModelListBoxEditor<OpenStackApiVersionType> authApiVersionEditor;

    @UiField
    @Path(value = "username.entity")
    @WithElementId
    StringEntityModelTextBoxEditor usernameEditor;

    @UiField
    @Path(value = "password.entity")
    @WithElementId
    StringEntityModelPasswordBoxEditor passwordEditor;

    @UiField(provided = true)
    @Path(value = "authProtocol.selectedItem")
    @WithElementId
    ListModelListBoxEditor<OpenStackProtocolType> authProtocolEditor;

    @UiField
    @Path(value = "authHostname.entity")
    @WithElementId
    StringEntityModelTextBoxEditor authHostnameEditor;

    @UiField(provided = true)
    @Path(value = "authPort.entity")
    @WithElementId
    StringEntityModelTextBoxEditor authPortEditor;

    @UiField
    @Path(value = "userDomainName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor userDomainNameEditor;

    @UiField
    @Path(value = "projectName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor projectNameEditor;

    @UiField
    @Path(value = "projectDomainName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor projectDomainNameEditor;

    @UiField
    @Path(value = "tenantName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor tenantNameEditor;

    @UiField
    @Path(value = "pluginType.selectedItem")
    @WithElementId
    ListModelSuggestBoxEditor pluginTypeEditor;

    @UiField
    @WithElementId
    FlowPanel networkingPanel;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    Row typeEditorRow;

    @UiField
    Row datacenterEditorRow;

    @UiField
    @Ignore
    VmwarePropertiesWidget vmwarePropertiesWidget;

    @UiField
    @Ignore
    KVMPropertiesWidget kvmPropertiesWidget;

    @UiField
    @Ignore
    XENPropertiesWidget xenPropertiesWidget;

    @UiField
    @Ignore
    KubevirtPropertiesWidget kubevirtPropertiesWidget;

    @UiField(provided = true)
    @Path(value = "readOnly.entity")
    @WithElementId
    EntityModelCheckBoxEditor readOnlyEditor;

    @UiField
    Style style;

    private ProviderModel providerModel;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Inject
    public ProviderPopupView(EventBus eventBus) {
        super(eventBus);

        authPortEditor = StringEntityModelTextBoxEditor.newTrimmingEditor();
        typeEditor = new ListModelListBoxEditor<>(new EnumRenderer());
        authApiVersionEditor = new ListModelListBoxEditor<>(new NameRenderer());
        autoSyncEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        isUnmanagedEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        datacenterEditor = new ListModelListBoxEditor<>(new AbstractRenderer<StoragePool>() {
            @Override
            public String render(StoragePool storagePool) {
                return storagePool != null ? storagePool.getName() :
                    ConstantsManager.getInstance().getConstants().anyDataCenter();
            }
        });
        requiresAuthenticationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        authProtocolEditor = new ListModelListBoxEditor<>(new NameRenderer());
        readOnlyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(ProviderModel model) {
        providerModel = model;
        driver.edit(model);
        vmwarePropertiesWidget.edit(model.getVmwarePropertiesModel());
        kvmPropertiesWidget.edit(model.getKvmPropertiesModel());
        xenPropertiesWidget.edit(model.getXenPropertiesModel());
        kubevirtPropertiesWidget.edit(model.getKubevirtPropertiesModel());

        if (model.isEditProviderMode()) {
            setCurrentActiveProviderWidget();
        }

        updatePasswordTitle();
    }

    @Override
    public ProviderModel flush() {
        vmwarePropertiesWidget.flush();
        kvmPropertiesWidget.flush();
        xenPropertiesWidget.flush();
        kubevirtPropertiesWidget.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    interface Style extends CssResource {
        String contentStyle();
        String headerSeparator();
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

    @Override
    public void setTestResult(String errorMessage) {
        testResultMessage.clearMessages();
        testResultMessage.setVisible(true);
        if (errorMessage == null || errorMessage.isEmpty()) {
            testResultMessage.setType(AlertPanel.Type.SUCCESS);
            testResultMessage.addMessage(SafeHtmlUtils.fromSafeConstant(constants.testSuccessMessage()));
        } else {
            testResultMessage.setType(AlertPanel.Type.DANGER);
            testResultMessage.addMessage(SafeHtmlUtils.fromString(errorMessage));

        }
    }

    @Override
    public void setCurrentActiveProviderWidget() {
        if (providerModel != null) {
            if (providerModel.getDataCenter().getIsAvailable()) {
                typeEditorRow.removeStyleName(style.headerSeparator());
                datacenterEditorRow.addStyleName(style.headerSeparator());
            } else {
                typeEditorRow.addStyleName(style.headerSeparator());
                datacenterEditorRow.removeStyleName(style.headerSeparator());
            }
            networkingPanel.setVisible(providerModel.getNeutronAgentModel().getIsAvailable());
            kvmPropertiesWidget.setVisible(providerModel.getKvmPropertiesModel().getIsAvailable());
            vmwarePropertiesWidget.setVisible(providerModel.getVmwarePropertiesModel().getIsAvailable());
            xenPropertiesWidget.setVisible(providerModel.getXenPropertiesModel().getIsAvailable());
            kubevirtPropertiesWidget.setVisible(providerModel.getKubevirtPropertiesModel().getIsAvailable());
        }
    }

    @Override public void updatePasswordTitle() {
        if (providerModel.getType() == null || providerModel.getType().getSelectedItem() == null) {
            return;
        }

        String passwordLabel;
        switch (providerModel.getType().getSelectedItem()) {
        case KUBEVIRT:
            passwordLabel = constants.kubevirtToken();
            break;
        default:
            passwordLabel = constants.passwordProvider();
            break;
        }
        passwordEditor.setLabel(passwordLabel);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        nameEditor.setTabIndex(nextTabIndex++);
        descriptionEditor.setTabIndex(nextTabIndex++);
        typeEditor.setTabIndex(nextTabIndex++);
        datacenterEditor.setTabIndex(nextTabIndex++);
        pluginTypeEditor.setTabIndex(nextTabIndex++);
        autoSyncEditor.setTabIndex(nextTabIndex++);
        isUnmanagedEditor.setTabIndex(nextTabIndex++);
        urlEditor.setTabIndex(nextTabIndex++);
        readOnlyEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = vmwarePropertiesWidget.setTabIndexes(nextTabIndex);
        kvmPropertiesWidget.setTabIndexes(nextTabIndex++);
        xenPropertiesWidget.setTabIndexes(nextTabIndex++);
        requiresAuthenticationEditor.setTabIndex(nextTabIndex++);
        usernameEditor.setTabIndex(nextTabIndex++);
        passwordEditor.setTabIndex(nextTabIndex++);
        authProtocolEditor.setTabIndexes(nextTabIndex++);
        authHostnameEditor.setTabIndexes(nextTabIndex++);
        authPortEditor.setTabIndexes(nextTabIndex++);
        authApiVersionEditor.setTabIndexes(nextTabIndex++);
        userDomainNameEditor.setTabIndexes(nextTabIndex++);
        projectNameEditor.setTabIndexes(nextTabIndex++);
        projectDomainNameEditor.setTabIndexes(nextTabIndex++);
        tenantNameEditor.setTabIndex(nextTabIndex++);
        nextTabIndex = kubevirtPropertiesWidget.setTabIndexes(nextTabIndex++);
        testButton.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}
