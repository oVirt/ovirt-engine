package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.InstallModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostPopupView.Style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.Editor.Path;
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
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Path(value = "hostVersion.entity")
    EntityModelLabelEditor hostVersionEditor;

    @UiField(provided = true)
    @Path(value = "OVirtISO.selectedItem")
    ListModelListBoxEditor<Object> isoEditor;

    @UiField
    @Path(value = "overrideIpTables.entity")
    @WithElementId("overrideIpTables")
    EntityModelCheckBoxEditor overrideIpTablesEditor;

    @UiField
    Label message;

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
    EntityModelTextBoxEditor userNameEditor;

    @UiField(provided = true)
    @Path(value = "publicKey.entity")
    @WithElementId("publicKey")
    EntityModelTextAreaLabelEditor publicKeyEditor;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public HostInstallPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles();
        driver.initialize(this);
    }

    void initListBoxEditors() {
        isoEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {

                // Format string to contain major.minor version only.
                RpmVersion version = (RpmVersion) object;

                return version.getRpmName();
            }
        });

        rbPassword = new RadioButton("1"); //$NON-NLS-1$
        rbPublicKey = new RadioButton("1"); //$NON-NLS-1$
        publicKeyEditor = new EntityModelTextAreaLabelEditor(true, true);
    }

    void localize(ApplicationConstants constants) {
        hostVersionEditor.setLabel(constants.hostInstallHostVersionLabel());
        isoEditor.setLabel(constants.hostInstallIsoLabel());
        overrideIpTablesEditor.setLabel(constants.hostInstallOverrideIpTablesLabel());
        authLabel.setText(constants.hostPopupAuthLabel());
        userNameEditor.setLabel(constants.hostPopupUsernameLabel());
    }

    @Override
    public void edit(final InstallModel model) {
        driver.edit(model);

        rbPublicKey.setValue(true);
        model.setAuthenticationMethod(AuthenticationMethod.PublicKey);
        displayPassPkWindow(false);

        rbPassword.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                model.setAuthenticationMethod(AuthenticationMethod.Password);
                displayPassPkWindow(true);
            }
        });

        rbPublicKey.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                model.setAuthenticationMethod(AuthenticationMethod.PublicKey);
                displayPassPkWindow(false);
            }
        });
        // TODO: remove setIsChangable when configured ssh username is enabled
        userNameEditor.setEnabled(false);
    }

    private void displayPassPkWindow(boolean isPasswordVisible) {
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
        String overrideIpStyle();
    }

    private void addStyles() {
        overrideIpTablesEditor.addContentWidgetStyleName(style.overrideIpStyle());
    }
}
