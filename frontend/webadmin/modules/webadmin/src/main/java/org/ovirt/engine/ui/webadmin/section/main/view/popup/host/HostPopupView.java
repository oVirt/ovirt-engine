package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.HasUiCommandClickHandlers;
import org.ovirt.engine.ui.webadmin.widget.UiCommandButton;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.webadmin.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostPopupView extends AbstractModelBoundPopupView<HostModel> implements HostPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostModel, HostPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    DialogTab generalTab;

    @UiField
    DialogTab powerManagementTab;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField
    @Path(value = "name.entity")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "host.entity")
    EntityModelTextBoxEditor hostAddressEditor;

    @UiField
    @Path(value = "rootPassword.entity")
    EntityModelPasswordBoxEditor rootPasswordEditor;

    @UiField(provided = true)
    @Path(value = "isPm.entity")
    EntityModelCheckBoxEditor pmEnabledEditor;

    @UiField
    @Path(value = "managementIp.entity")
    EntityModelTextBoxEditor pmAddressEditor;

    @UiField
    @Path(value = "pmUserName.entity")
    EntityModelTextBoxEditor pmUserNameEditor;

    @UiField
    @Path(value = "pmPassword.entity")
    EntityModelPasswordBoxEditor pmPasswordEditor;

    @UiField(provided = true)
    @Path(value = "pmType.selectedItem")
    ListModelListBoxEditor<Object> pmTypeEditor;

    @UiField
    @Path(value = "pmPort.entity")
    EntityModelTextBoxEditor pmPortEditor;

    @UiField
    @Path(value = "pmSlot.entity")
    EntityModelTextBoxEditor pmSlotEditor;

    @UiField
    @Path(value = "pmOptions.entity")
    EntityModelTextBoxEditor pmOptionsEditor;

    @UiField
    @Ignore
    Label pmOptionsExplanationLabel;

    @UiField
    @Path(value = "pmSecure.entity")
    EntityModelCheckBoxEditor pmSecureEditor;

    @UiField
    UiCommandButton testButton;

    @UiField
    @Ignore
    Label testMessage;

    @Inject
    public HostPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initCheckBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_pool) object).getname();
            }
        });

        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });

        pmTypeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            protected String renderNullSafe(Object object) {
                return (String) object;
            }
        });
    }

    void initCheckBoxEditors() {
        pmEnabledEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(ApplicationConstants constants) {
        // General tab
        generalTab.setLabel(constants.hostPopupGeneralTabLabel());
        dataCenterEditor.setLabel(constants.hostPopupDataCenterLabel());
        clusterEditor.setLabel(constants.hostPopupClusterLabel());
        nameEditor.setLabel(constants.hostPopupNameLabel());
        hostAddressEditor.setLabel(constants.hostPopupHostAddressLabel());
        rootPasswordEditor.setLabel(constants.hostPopupRootPasswordLabel());

        // Power Management tab
        powerManagementTab.setLabel(constants.hostPopupPowerManagementTabLabel());
        pmEnabledEditor.setLabel(constants.hostPopupPmEnabledLabel());
        pmAddressEditor.setLabel(constants.hostPopupPmAddressLabel());
        pmUserNameEditor.setLabel(constants.hostPopupPmUserNameLabel());
        pmPasswordEditor.setLabel(constants.hostPopupPmPasswordLabel());
        pmTypeEditor.setLabel(constants.hostPopupPmTypeLabel());
        pmPortEditor.setLabel(constants.hostPopupPmPortLabel());
        pmSlotEditor.setLabel(constants.hostPopupPmSlotLabel());
        pmOptionsEditor.setLabel(constants.hostPopupPmOptionsLabel());
        pmOptionsExplanationLabel.setText(constants.hostPopupPmOptionsExplanationLabel());
        pmSecureEditor.setLabel(constants.hostPopupPmSecureLabel());
        testButton.setLabel(constants.hostPopupTestButtonLabel());
    }

    @Override
    public void setMessage(String message) {
        testMessage.setText(message);
    }

    @Override
    public void edit(final HostModel object) {
        Driver.driver.edit(object);

        // TODO should be handled in a more generic way
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;

                if ("IsGeneralTabValid".equals(propName)) {
                    if (object.getIsGeneralTabValid()) {
                        generalTab.markAsValid();
                    } else {
                        generalTab.markAsInvalid(null);
                    }
                } else if ("IsPowerManagementTabValid".equals(propName)) {
                    if (object.getIsPowerManagementTabValid()) {
                        powerManagementTab.markAsValid();
                    } else {
                        powerManagementTab.markAsInvalid(null);
                    }
                }
            }
        });

        testButton.setCommand(object.getTestCommand());
    }

    @Override
    public HostModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focus() {
        nameEditor.setFocus(true);
    }

    @Override
    public HasUiCommandClickHandlers getTestButton() {
        return testButton;
    }

    @Override
    public void showPowerManagement() {
        tabPanel.switchTab(powerManagementTab);
    }

}
