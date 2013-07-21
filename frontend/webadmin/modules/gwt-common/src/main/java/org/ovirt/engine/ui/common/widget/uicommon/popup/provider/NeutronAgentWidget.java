package org.ovirt.engine.ui.common.widget.uicommon.popup.provider;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.EntityModelWidgetWithInfo;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelPasswordBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.NeutronAgentModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
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

    @UiField(provided = true)
    EntityModelWidgetWithInfo mappings;

    @Path(value = "interfaceMappingsLabel.entity")
    @WithElementId("interfaceMappingsLabel")
    EntityModelLabel mappingsLabel;

    @Path(value = "interfaceMappings.entity")
    @WithElementId("interfaceMappings")
    EntityModelTextBoxOnlyEditor interfaceMappings;

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

    private final CommonApplicationTemplates templates;

    @Inject
    public NeutronAgentWidget(CommonApplicationConstants constants,
            CommonApplicationResources resources,
            CommonApplicationTemplates templates) {

        this.templates = templates;

        mappingsLabel = new EntityModelLabel();
        interfaceMappings = new EntityModelTextBoxOnlyEditor();
        mappings = new EntityModelWidgetWithInfo(mappingsLabel, interfaceMappings);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);

        qpidHost.setLabel(constants.hostQpid());
        qpidPort.setLabel(constants.portQpid());
        qpidUsername.setLabel(constants.usernameQpid());
        qpidPassword.setLabel(constants.passwordQpid());

        driver.initialize(this);
    }

    @Override
    public void edit(final NeutronAgentModel model) {
        driver.edit(model);
        mappings.setExplanation(templates.italicText((String) model.getInterfaceMappingsExplanation().getEntity()));
        model.getInterfaceMappingsExplanation().getEntityChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                mappings.setExplanation(templates.italicText((String) model.getInterfaceMappingsExplanation()
                        .getEntity()));
            }
        });
    }

    @Override
    public NeutronAgentModel flush() {
        return driver.flush();
    }

}
