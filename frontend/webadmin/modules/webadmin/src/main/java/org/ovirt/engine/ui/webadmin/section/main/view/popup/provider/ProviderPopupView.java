package org.ovirt.engine.ui.webadmin.section.main.view.popup.provider;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.provider.ProviderPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ProviderPopupView extends AbstractModelBoundPopupView<ProviderModel> implements ProviderPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ProviderModel, ProviderPopupView> {}

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ProviderPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ProviderPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "type.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> typeEditor;

    @UiField
    @Path(value = "url.entity")
    @WithElementId
    EntityModelTextBoxEditor urlEditor;

    @UiField
    @Path(value = "apiVersion.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> apiVersionEditor;

    @UiField
    UiCommandButton testButton;

    @UiField
    Image testResultImage;

    @UiField
    @Ignore
    Label testResultMessage;

    @UiField(provided = true)
    @Path(value = "requiresAuthentication.entity")
    @WithElementId
    EntityModelCheckBoxEditor requiresAuthenticationEditor;

    @UiField
    @Path(value = "username.entity")
    @WithElementId
    EntityModelTextBoxEditor usernameEditor;

    @UiField
    @Path(value = "password.entity")
    @WithElementId
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Path(value = "tenantName.entity")
    @WithElementId
    EntityModelTextBoxEditor tenantNameEditor;

    @UiField
    @Path(value = "pluginType.selectedItem")
    @WithElementId
    ListModelSuggestBoxEditor pluginTypeEditor;

    @UiField
    @WithElementId
    DialogTab generalTab;

    @UiField
    @Ignore
    DialogTab agentConfigurationTab;

    @UiField
    @Path(value = "host.entity")
    @WithElementId("host")
    EntityModelTextBoxEditor host;

    @UiField
    @Path(value = "qpidPort.entity")
    @WithElementId("qpidPort")
    EntityModelTextBoxEditor qpidPort;

    @UiField
    @Path(value = "userName.entity")
    @WithElementId("userName")
    EntityModelTextBoxEditor userName;

    @UiField
    @Path(value = "agentConfigPassword.entity")
    @WithElementId("agentConfigPassword")
    EntityModelPasswordBoxEditor agentConfigPassword;

    @UiField
    @Path(value = "interfaceMappings.entity")
    @WithElementId("interfaceMappings")
    EntityModelTextBoxEditor interfaceMappings;

    @UiField
    Style style;

    private final ApplicationResources resources;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Inject
    public ProviderPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);

        typeEditor = new ListModelListBoxEditor<Object>(new EnumRenderer());
        requiresAuthenticationEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        this.resources = resources;
        this.constants = constants;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addContentStyleName(style.contentStyle());
        driver.initialize(this);
        apiVersionEditor.asListBox().addStyleName(style.apiVersionStyle());
    }

    void localize(ApplicationConstants constants) {
        // General tab
        generalTab.setLabel(constants.providerPopupGeneralTabLabel());
        nameEditor.setLabel(constants.nameProvider());
        descriptionEditor.setLabel(constants.descriptionProvider());
        typeEditor.setLabel(constants.typeProvider());
        urlEditor.setLabel(constants.urlProvider());
        testButton.setLabel(constants.testProvider());
        requiresAuthenticationEditor.setLabel(constants.requiresAuthenticationProvider());
        usernameEditor.setLabel(constants.usernameProvider());
        passwordEditor.setLabel(constants.passwordProvider());
        tenantNameEditor.setLabel(constants.tenantName());
        pluginTypeEditor.setLabel(constants.pluginType());

        // Agent configuration tab
        agentConfigurationTab.setLabel(constants.providerPopupAgentConfigurationTabLabel());
        host.setLabel(constants.hostQpid());
        qpidPort.setLabel(constants.portQpid());
        userName.setLabel(constants.usernameQpid());
        agentConfigPassword.setLabel(constants.passwordQpid());
    }

    @Override
    public void edit(ProviderModel model) {
        customizeAgentTab((Boolean) model.getAgentTabAvailable().getEntity(),
                (String) model.getInterfaceMappingsLabel().getEntity());
        driver.edit(model);
    }

    @Override
    public ProviderModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    public void addContentStyleName(String styleName) {
        this.asWidget().addContentStyleName(styleName);
    }

    interface Style extends CssResource {
        String contentStyle();
        String apiVersionStyle();
        String testResultImage();
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

    @Override
    public void setTestResultImage(String errorMessage) {
        testResultImage.setResource(errorMessage.isEmpty() ? resources.logNormalImage() : resources.logErrorImage());
        testResultImage.setStylePrimaryName(style.testResultImage());
        testResultMessage.setText(errorMessage.isEmpty() ? constants.testSuccessMessage() : errorMessage);
    }

    @Override
    public void customizeAgentTab(boolean tabAvailable, String ifMappingsLabel) {
        agentConfigurationTab.setVisible(tabAvailable);
        interfaceMappings.setLabel(ifMappingsLabel);
    }

}
