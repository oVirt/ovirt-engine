package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.gluster.ReplaceBrickModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.ReplaceBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class ReplaceBrickPopupView extends AbstractModelBoundPopupView<ReplaceBrickModel> implements ReplaceBrickPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ReplaceBrickModel, ReplaceBrickPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ReplaceBrickPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ReplaceBrickPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "servers.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> serverEditor;

    @UiField
    @Path(value = "brickDirectory.entity")
    @WithElementId
    EntityModelTextBoxEditor brickDirEditor;

    @Inject
    public ReplaceBrickPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        Driver.driver.initialize(this);
    }

    private void initListBoxEditors() {
        serverEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).getHostName();
            }
        });

    }

    private void localize(ApplicationConstants constants) {
        serverEditor.setLabel(constants.serverBricks());
        brickDirEditor.setLabel(constants.brickDirectoryBricks());
    }

    @Override
    public void edit(ReplaceBrickModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public ReplaceBrickModel flush() {
        return Driver.driver.flush();
    }
}
