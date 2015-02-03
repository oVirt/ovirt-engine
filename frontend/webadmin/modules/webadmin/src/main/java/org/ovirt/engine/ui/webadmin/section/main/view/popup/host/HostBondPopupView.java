package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ListModelSuggestBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostBondInterfaceModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostBondPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.editor.EnumRadioEditor;

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

import java.util.Map;

public class HostBondPopupView extends AbstractModelBoundPopupView<HostBondInterfaceModel> implements HostBondPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostBondInterfaceModel, HostBondPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostBondPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @Path(value = "bond.selectedItem")
    ListModelSuggestBoxEditor bondSuggestEditor;

    @UiField(provided = true)
    @Path(value = "bond.selectedItem")
    ListModelListBoxEditor<String> bondEditor;

    @UiField(provided = true)
    @Path(value = "network.selectedItem")
    ListModelListBoxEditor<Network> networkEditor;

    @UiField(provided = true)
    @Path(value = "bondingOptions.selectedItem")
    ListModelListBoxEditor<Map.Entry<String, EntityModel<String>>> bondingModeEditor;

    @UiField
    @Ignore
    StringEntityModelTextBoxEditor customEditor;

    @UiField
    @Ignore
    NicLabelWidget labelsWidget;

    @UiField(provided = true)
    EnumRadioEditor<NetworkBootProtocol> bootProtocol;

    @UiField
    @Ignore
    StringEntityModelLabelEditor bootProtocolLabel;

    @UiField
    @Path(value = "address.entity")
    StringEntityModelTextBoxEditor address;

    @UiField
    @Path(value = "subnet.entity")
    StringEntityModelTextBoxEditor subnet;

    @UiField
    @Path(value = "gateway.entity")
    StringEntityModelTextBoxEditor gateway;

    @UiField(provided = true)
    @Path(value = "checkConnectivity.entity")
    EntityModelCheckBoxEditor checkConnectivity;

    @UiField
    @Ignore
    Label message;

    @UiField
    @Ignore
    HTML info;

    @UiField
    @Ignore
    DockLayoutPanel layoutPanel;

    @UiField
    @Ignore
    VerticalPanel mainPanel;

    @UiField
    @Ignore
    VerticalPanel infoPanel;

    @UiField(provided = true)
    @Path(value = "commitChanges.entity")
    EntityModelCheckBoxEditor commitChanges;

    @UiField
    Style style;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public HostBondPopupView(EventBus eventBus, ApplicationResources resources, final ApplicationConstants constants) {
        super(eventBus, resources);

        bondSuggestEditor = new ListModelSuggestBoxEditor();
        bondEditor = new ListModelListBoxEditor<String>();
        networkEditor = new ListModelListBoxEditor<Network>(new NullSafeRenderer<Network>() {
            @Override
            protected String renderNullSafe(Network network) {
                return network.getName();
            }

        });
        bondingModeEditor = new ListModelListBoxEditor<Map.Entry<String, EntityModel<String>>>(new NullSafeRenderer<Map.Entry<String, EntityModel<String>>>() {
            @SuppressWarnings("unchecked")
            @Override
            protected String renderNullSafe(Map.Entry<String, EntityModel<String>> pair) {
                String key = pair.getKey();
                String value = pair.getValue().getEntity();
                if ("custom".equals(key)) { //$NON-NLS-1$
                    return constants.customHostPopup() + ": " + value; //$NON-NLS-1$
                }
                return value;
            }
        });
        bootProtocol = new EnumRadioEditor<NetworkBootProtocol>(NetworkBootProtocol.class);

        checkConnectivity = new EntityModelCheckBoxEditor(Align.RIGHT);
        commitChanges = new EntityModelCheckBoxEditor(Align.RIGHT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        // Set Styles
        checkConnectivity.setContentWidgetContainerStyleName(style.checkCon());
        mainPanel.getElement().setPropertyString("width", "100%"); //$NON-NLS-1$ //$NON-NLS-2$

        // Localize
        bondSuggestEditor.setLabel(constants.bondNameHostPopup() + ":"); //$NON-NLS-1$
        bondEditor.setLabel(constants.bondNameHostPopup() + ":"); //$NON-NLS-1$
        networkEditor.setLabel(constants.networkHostPopup() + ":"); //$NON-NLS-1$
        bondingModeEditor.setLabel(constants.bondingModeHostPopup() + ":"); //$NON-NLS-1$
        customEditor.setLabel(constants.customModeHostPopup() + ":"); //$NON-NLS-1$
        bootProtocolLabel.setLabel(constants.bootProtocolHostPopup() + ":"); //$NON-NLS-1$
        bootProtocolLabel.asEditor().getSubEditor().setValue("   "); //$NON-NLS-1$
        address.setLabel(constants.ipHostPopup() + ":"); //$NON-NLS-1$
        subnet.setLabel(constants.subnetMaskHostPopup() + ":"); //$NON-NLS-1$
        gateway.setLabel(constants.gwHostPopup() + ":"); //$NON-NLS-1$
        checkConnectivity.setLabel(constants.checkConHostPopup());
        info.setHTML(constants.changesTempHostPopup());
        commitChanges.setLabel(constants.saveNetConfigHostPopup());

        driver.initialize(this);
    }

    @Override
    public void edit(final HostBondInterfaceModel object) {
        driver.edit(object);

        bondSuggestEditor.setVisible(false);

        if (!object.getBootProtocolAvailable()) {
            bootProtocol.asWidget().setVisible(false);
            bootProtocolLabel.setVisible(false);
        }
        bootProtocol.setEnabled(NetworkBootProtocol.NONE, object.getNoneBootProtocolAvailable());
        updateBondOptions(object.getBondingOptions());

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                HostBondInterfaceModel model = (HostBondInterfaceModel) sender;
                String propertyName = args.propertyName;
                if ("NoneBootProtocolAvailable".equals(propertyName)) { //$NON-NLS-1$
                    bootProtocol.setEnabled(NetworkBootProtocol.NONE, model.getNoneBootProtocolAvailable());
                }
                else if ("Message".equals(propertyName)) { //$NON-NLS-1$
                    message.setText(model.getMessage());
                }
            }
        });

        object.getBondingOptions().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                ListModel<Map.Entry<String, EntityModel<String>>> list = (ListModel<Map.Entry<String, EntityModel<String>>>) sender;
                updateBondOptions(list);
            }
        });

        customEditor.asValueBox().addValueChangeHandler(new ValueChangeHandler<String>() {
            @SuppressWarnings("unchecked")
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                for (Map.Entry<String, EntityModel<String>> pair : object.getBondingOptions().getItems()) {
                    if ("custom".equals(pair.getKey())) { //$NON-NLS-1$
                        pair.getValue().setEntity(event.getValue());
                    }
                }
            }
        });

        bondingModeEditor.setVisible(true);
        bondingModeEditor.asWidget().setVisible(true);
    }

    @Override
    public HostBondInterfaceModel flush() {
        return driver.flush();
    }

    @Override
    public void focusInput() {
        networkEditor.setFocus(true);
    }

    @Override
    public void setMessage(String message) {
    }

    private void updateBondOptions(ListModel<Map.Entry<String, EntityModel<String>>> list) {
        @SuppressWarnings("unchecked")
        Map.Entry<String, EntityModel<String>> pair = list.getSelectedItem();
        if ("custom".equals(pair.getKey())) { //$NON-NLS-1$
            customEditor.setVisible(true);
            String entity = pair.getValue().getEntity();
            customEditor.asEditor().getSubEditor().setValue(entity == null ? "" : entity); //$NON-NLS-1$
        } else {
            customEditor.setVisible(false);
        }
    }

    interface Style extends CssResource {

        String checkCon();
    }

}
