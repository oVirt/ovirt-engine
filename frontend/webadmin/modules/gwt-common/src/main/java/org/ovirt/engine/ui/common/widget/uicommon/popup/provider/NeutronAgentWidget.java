package org.ovirt.engine.ui.common.widget.uicommon.popup.provider;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.NeutronAgentModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class NeutronAgentWidget extends AbstractModelBoundPopupWidget<NeutronAgentModel> {

    interface Driver extends SimpleBeanEditorDriver<NeutronAgentModel, NeutronAgentWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, NeutronAgentWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NeutronAgentWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
    }

    @UiField
    @Path(value = "interfaceMappings.entity")
    @WithElementId("interfaceMappings")
    EntityModelTextBoxEditor interfaceMappings;

    @UiField
    @Ignore
    Label qpidTitle;

    @UiField
    @Path(value = "qpidHost.entity")
    @WithElementId("qpidHost")
    EntityModelTextBoxEditor qpidHost;

    @UiField
    @Path(value = "qpidPort.entity")
    @WithElementId("qpidPort")
    EntityModelTextBoxEditor qpidPort;

    @UiField
    @Path(value = "qpidUsername.entity")
    @WithElementId("qpidUsername")
    EntityModelTextBoxEditor qpidUsername;

    @UiField
    @Path(value = "qpidPassword.entity")
    @WithElementId("qpidPassword")
    EntityModelPasswordBoxEditor qpidPassword;

    @UiField
    Style style;

    @Inject
    public NeutronAgentWidget(CommonApplicationConstants constants) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        qpidTitle.setText(constants.qpid());
        qpidHost.setLabel(constants.hostQpid());
        qpidPort.setLabel(constants.portQpid());
        qpidUsername.setLabel(constants.usernameQpid());
        qpidPassword.setLabel(constants.passwordQpid());

        driver.initialize(this);
    }

    @Override
    public void edit(final NeutronAgentModel model) {
        qpidTitle.setVisible(model.getIsAvailable());
        interfaceMappings.setLabel((String) model.getInterfaceMappingsLabel().getEntity());
        model.getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) { //$NON-NLS-1$
                    qpidTitle.setVisible(model.getIsAvailable());
                }
            }
        });
        model.getInterfaceMappingsLabel().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                interfaceMappings.setLabel((String) model.getInterfaceMappingsLabel().getEntity());
            }
        });
        driver.edit(model);
    }

    @Override
    public NeutronAgentModel flush() {
        return driver.flush();
    }

}
