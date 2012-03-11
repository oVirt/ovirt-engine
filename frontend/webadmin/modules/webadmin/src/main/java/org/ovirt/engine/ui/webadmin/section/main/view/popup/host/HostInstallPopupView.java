package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.hosts.InstallModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;

/**
 * This is the dialog used to re-install a host.
 * <p/>
 * Take into account that it can be used both for a normal host an also for an bare metal hypervisor. In the first case
 * it will ask for the root password and in the second it will as for the location of the ISO image of the hypervisor.
 */
public class HostInstallPopupView extends AbstractModelBoundPopupView<InstallModel> implements HostInstallPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<InstallModel, HostInstallPopupView> {

        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostInstallPopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "rootPassword.entity")
    EntityModelPasswordBoxEditor passwordEditor;

    @UiField
    @Path(value = "hostVersion.entity")
    EntityModelLabelEditor hostVersionEditor;

    @UiField(provided = true)
    @Path(value = "oVirtISO.selectedItem")
    ListModelListBoxEditor<Object> isoEditor;

    @UiField
    @Path(value = "OverrideIpTables.entity")
    EntityModelCheckBoxEditor overrideIpTablesEditor;

    @UiField
    Label message;

    @Inject
    public HostInstallPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        isoEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {

                // Format string to contain major.minor version only.
                RpmVersion version = (RpmVersion) object;

                return version.getMajor() + "." + version.getMinor();
            }
        });
    }

    void localize(ApplicationConstants constants) {
        passwordEditor.setLabel(constants.hostInstallPasswordLabel());
        hostVersionEditor.setLabel(constants.hostInstallHostVersionLabel());
        isoEditor.setLabel(constants.hostInstallIsoLabel());
        overrideIpTablesEditor.setLabel(constants.hostInstallOverrideIpTablesLabel());
    }

    @Override
    public void edit(final InstallModel model) {
        Driver.driver.edit(model);
    }

    @Override
    public InstallModel flush() {
        return Driver.driver.flush();
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

}
