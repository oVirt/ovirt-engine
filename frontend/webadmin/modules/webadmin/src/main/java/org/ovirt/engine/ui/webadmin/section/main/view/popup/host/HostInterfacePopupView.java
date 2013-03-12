package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.editor.EnumRadioEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class HostInterfacePopupView extends AbstractModelBoundPopupView<HostInterfaceModel> implements HostInterfacePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostInterfaceModel, HostInterfacePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostInterfacePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    EntityModelLabelEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "network.selectedItem")
    ListModelListBoxEditor<Object> networkEditor;

    @UiField(provided = true)
    @Path(value = "bondingOptions.selectedItem")
    ListModelListBoxEditor<Object> bondingModeEditor;

    @UiField
    @Ignore
    EntityModelTextBoxEditor customEditor;

    @UiField(provided = true)
    EnumRadioEditor<NetworkBootProtocol> bootProtocol;

    @UiField
    @Ignore
    EntityModelLabelEditor bootProtocolLabel;

    @UiField
    @Path(value = "address.entity")
    EntityModelTextBoxEditor address;

    @UiField
    @Path(value = "subnet.entity")
    EntityModelTextBoxEditor subnet;

    @UiField(provided = true)
    @Path(value = "checkConnectivity.entity")
    EntityModelCheckBoxEditor checkConnectivity;

    @UiField(provided = true)
    @Path(value = "isToSync.entity")
    EntityModelCheckBoxEditor isToSync;

    @UiField(provided = true)
    InfoIcon isToSyncInfo;

    @UiField
    @Ignore
    Label message;

    @UiField
    @Ignore
    DockLayoutPanel layoutPanel;

    @UiField
    @Ignore
    VerticalPanel mainPanel;

    @UiField
    @Ignore
    VerticalPanel infoPanel;

    @UiField
    @Ignore
    HTML info;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public HostInterfacePopupView(EventBus eventBus, ApplicationResources resources, final ApplicationConstants constants, final ApplicationTemplates templates) {
        super(eventBus, resources);

        networkEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            protected String renderNullSafe(Object object) {
                return ((Network) object).getName();
            }

        });
        bondingModeEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            protected String renderNullSafe(Object object) {
                KeyValuePairCompat<String, EntityModel> pair = (KeyValuePairCompat<String, EntityModel>) object;
                String key = pair.getKey();
                if ("custom".equals(key)) { //$NON-NLS-1$
                    return constants.customHostPopup() + ":"; //$NON-NLS-1$
                }
                EntityModel value = pair.getValue();
                return (String) value.getEntity();
            }
        });
        bootProtocol = new EnumRadioEditor<NetworkBootProtocol>(NetworkBootProtocol.class, eventBus);

        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSync = new EntityModelCheckBoxEditor(Align.RIGHT);
        isToSyncInfo = new InfoIcon(templates.italicTwoLines(constants.syncNetworkInfoPart1(), constants.syncNetworkInfoPart2()), resources);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        // Set Styles
        isToSync.setContentWidgetStyleName(style.syncInfo());
        mainPanel.getElement().setPropertyString("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        // Localize
        nameEditor.setLabel(constants.nameHostPopup() + ":"); //$NON-NLS-1$
        networkEditor.setLabel(constants.networkHostPopup() + ":"); //$NON-NLS-1$
        bondingModeEditor.setLabel(constants.bondingModeHostPopup() + ":"); //$NON-NLS-1$
        bootProtocolLabel.setLabel(constants.bootProtocolHostPopup() +":"); //$NON-NLS-1$
        bootProtocolLabel.asEditor().getSubEditor().setValue("   "); //$NON-NLS-1$
        customEditor.setLabel(constants.customModeHostPopup() + ":"); //$NON-NLS-1$
        address.setLabel(constants.ipHostPopup() + ":"); //$NON-NLS-1$
        subnet.setLabel(constants.subnetMaskHostPopup() + ":"); //$NON-NLS-1$
        checkConnectivity.setLabel(constants.checkConHostPopup() + ":"); //$NON-NLS-1$
        info.setHTML(constants.changesTempHostPopup());
        isToSync.setLabel(constants.syncNetwork());
        commitChanges.setLabel(constants.saveNetConfigHostPopup());

        driver.initialize(this);
    }

    @Override
    public void edit(final HostInterfaceModel object) {
        driver.edit(object);
        object.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                HostInterfaceModel model = (HostInterfaceModel) sender;
                if ("BootProtocolsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    boolean bootProtocolsAvailable = model.getBootProtocolsAvailable();
                    bootProtocolLabel.setEnabled(bootProtocolsAvailable);
                    bootProtocol.setEnabled(bootProtocolsAvailable);
                    bootProtocol.setEnabled(NetworkBootProtocol.NONE, object.getNoneBootProtocolAvailable());
                    checkConnectivity.setEnabled(bootProtocolsAvailable);
                }
                if ("NoneBootProtocolAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    bootProtocol.setEnabled(NetworkBootProtocol.NONE, model.getNoneBootProtocolAvailable());
                }
                if ("Message".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    message.setText(model.getMessage());
                }
            }
        });

        object.getBondingOptions().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                ListModel list = (ListModel) sender;
                @SuppressWarnings("unchecked")
                KeyValuePairCompat<String, EntityModel> pair =
                        (KeyValuePairCompat<String, EntityModel>) list.getSelectedItem();
                if ("custom".equals(pair.getKey())) { //$NON-NLS-1$
                    customEditor.setVisible(true);
                    Object entity = pair.getValue().getEntity();
                    customEditor.asEditor().getSubEditor().setValue(entity == null ? "" : entity); //$NON-NLS-1$
                } else {
                    customEditor.setVisible(false);
                }
            }
        });

        customEditor.asValueBox().addValueChangeHandler(new ValueChangeHandler<Object>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onValueChange(ValueChangeEvent<Object> event) {
                for (Object item : object.getBondingOptions().getItems()) {
                    KeyValuePairCompat<String, EntityModel> pair = (KeyValuePairCompat<String, EntityModel>) item;
                    if ("custom".equals(pair.getKey())) { //$NON-NLS-1$
                        pair.getValue().setEntity(event.getValue());
                    }
                }
            }
        });

        bondingModeEditor.setVisible(true);
        bondingModeEditor.asWidget().setVisible(true);

        isToSync.setVisible(false);
        isToSyncInfo.setVisible(false);
    }

    @Override
    public HostInterfaceModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        networkEditor.setFocus(true);
    }

    @Override
    public void setMessage(String message) {
    }

    interface Style extends CssResource {

        String syncInfo();
    }

}
