package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.HostedEngineDeployConfiguration;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelRadioGroupEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.InstallModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.provider.HostNetworkProviderWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.inject.Inject;

/**
 * This is the dialog used to re-install a host.
 * <p/>
 * Take into account that it can be used both for a normal host an also for an bare metal hypervisor. In the first case
 * it will ask for the root password and in the second it will as for the location of the ISO image of the hypervisor.
 */
public class HostInstallPopupView extends AbstractModelBoundPopupView<InstallModel> implements HostInstallPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<InstallModel, HostInstallPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostInstallPopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Style style;

    @UiField
    @Path(value = "userPassword.entity")
    StringEntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Path(value = "hostVersion.entity")
    StringEntityModelLabelEditor hostVersionEditor;

    @UiField(provided = true)
    @Path(value = "OVirtISO.selectedItem")
    ListModelListBoxEditor<RpmVersion> isoEditor;

    @UiField(provided = true)
    @Path(value = "overrideIpTables.entity")
    @WithElementId("overrideIpTables")
    EntityModelCheckBoxEditor overrideIpTablesEditor;

    @UiField(provided = true)
    @Path(value = "activateHostAfterInstall.entity")
    @WithElementId("activateHostAfterInstall")
    EntityModelCheckBoxEditor activateHostAfterInstallEditor;

    @UiField
    @Ignore
    Label authLabel;

    @UiField(provided = true)
    @Ignore
    @WithElementId("rbPublicKey")
    public RadioButton rbPublicKey;

    @UiField(provided = true)
    @Ignore
    @WithElementId("rbPassword")
    public RadioButton rbPassword;

    @UiField
    @Path(value = "userName.entity")
    @WithElementId("userName")
    StringEntityModelTextBoxEditor userNameEditor;

    @UiField(provided = true)
    @Path(value = "publicKey.entity")
    @WithElementId("publicKey")
    StringEntityModelTextAreaLabelEditor publicKeyEditor;

    @UiField
    @Ignore
    @WithElementId("networkProviderWidget")
    HostNetworkProviderWidget networkProviderWidget;

    @UiField
    @Path(value = "hostedEngineHostModel.actions.selectedItem")
    ListModelRadioGroupEditor<HostedEngineDeployConfiguration.Action> hostedEngineDeployActionsEditor;

    @UiField
    @Ignore
    DialogTabPanel tabPanel;

    @UiField
    @Ignore
    DialogTab hostPopupGeneralTab;

    @UiField
    @Ignore
    DialogTab networkProviderTab;

    @UiField
    @Ignore
    DialogTab hostedEngineTab;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostInstallPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        hideLabels();
        localize();
        addStyles();
        driver.initialize(this);
        applyModeCustomizations();
    }

    private void hideLabels() {
        passwordEditor.hideLabel();
        publicKeyEditor.hideLabel();
    }

    private void applyModeCustomizations() {
        if (ApplicationModeHelper.getUiMode() == ApplicationMode.GlusterOnly) {
            networkProviderTab.setVisible(false);
        }
    }

    void initEditors() {
        isoEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<RpmVersion>() {
            @Override
            public String renderNullSafe(RpmVersion version) {
                // Format string to contain major.minor version only.
                return version.getRpmName();
            }
        });

        rbPassword = new RadioButton("1"); //$NON-NLS-1$
        rbPublicKey = new RadioButton("1"); //$NON-NLS-1$
        publicKeyEditor = new StringEntityModelTextAreaLabelEditor();
        activateHostAfterInstallEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        overrideIpTablesEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize() {
        hostVersionEditor.setLabel(constants.hostInstallHostVersionLabel());
        isoEditor.setLabel(constants.hostInstallIsoLabel());
        overrideIpTablesEditor.setLabel(constants.hostInstallOverrideIpTablesLabel());
        activateHostAfterInstallEditor.setLabel(constants.activateHostAfterInstallLabel());
        authLabel.setText(constants.hostPopupAuthLabel());
        userNameEditor.setLabel(constants.hostPopupUsernameLabel());
        publicKeyEditor.setTitle(constants.publicKeyUsage());
    }

    @Override
    public void edit(final InstallModel model) {
        driver.edit(model);
        model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                InstallModel installModel = (InstallModel) sender;

                if ("ValidationFailed".equals(args.propertyName)) { //$NON-NLS-1$
                    if (installModel.getValidationFailed().getEntity() != null &&
                            installModel.getValidationFailed().getEntity()) {
                        tabPanel.switchTab(hostPopupGeneralTab);
                    }
                }
            }
        });
        boolean installedFailed = model.getVds().getStatus() == VDSStatus.InstallFailed;
        model.setAuthenticationMethod(installedFailed ? AuthenticationMethod.Password: AuthenticationMethod.PublicKey);
        displayPasswordField(installedFailed);
        rbPassword.setValue(installedFailed);
        rbPublicKey.setValue(!installedFailed);

        rbPassword.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                model.setAuthenticationMethod(AuthenticationMethod.Password);
                displayPasswordField(true);
            }
        });

        rbPublicKey.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                model.setAuthenticationMethod(AuthenticationMethod.PublicKey);
                displayPasswordField(false);
            }
        });
        // TODO: remove setIsChangeable when configured ssh username is enabled
        userNameEditor.setEnabled(false);

        networkProviderWidget.edit(model.getNetworkProviderModel());
        if (model.getVds().isOvirtVintageNode()) {
            networkProviderTab.setVisible(false);
        }
    }

    private void displayPasswordField(boolean isPasswordVisible) {
        if (isPasswordVisible) {
            passwordEditor.getElement().getStyle().setVisibility(Visibility.VISIBLE);
            publicKeyEditor.getElement().getStyle().setVisibility(Visibility.HIDDEN);
        } else {
            passwordEditor.getElement().getStyle().setVisibility(Visibility.HIDDEN);
            publicKeyEditor.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        }
    }

    @Override
    public InstallModel flush() {
        networkProviderWidget.flush();
        return driver.flush();
    }

    @Override
    public void focusInput() {
        // We are trusting the model to decide which of the two alternatives of
        // the dialog (for a normal host or for a bare metal hypervisor):
        if (passwordEditor.isAccessible()) {
            passwordEditor.setFocus(true);
        }
        if (isoEditor.isAccessible()) {
            isoEditor.setFocus(true);
        }
    }

    interface Style extends CssResource {
        String pkStyle();
    }

    private void addStyles() {
        publicKeyEditor.setCustomStyle(style.pkStyle());
    }
}
