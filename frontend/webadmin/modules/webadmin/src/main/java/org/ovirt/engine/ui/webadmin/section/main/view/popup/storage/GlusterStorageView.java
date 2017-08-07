package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.gwtbootstrap3.client.ui.Row;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.storage.GlusterStorageModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class GlusterStorageView extends AbstractStorageView<GlusterStorageModel> {

    interface Driver extends UiCommonEditorDriver<GlusterStorageModel, GlusterStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, GlusterStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "path.entity")
    @WithElementId("path")
    StringEntityModelTextBoxEditor pathEditor;

    @UiField
    @Ignore
    Label pathExampleLabel;

    @UiField
    @Path(value = "vfsType.entity")
    @WithElementId("vfsType")
    StringEntityModelTextBoxEditor vfsTypeEditor;

    @UiField
    @Path(value = "mountOptions.entity")
    @WithElementId("mountOptions")
    StringEntityModelTextBoxEditor mountOptionsEditor;

    @UiField
    Row glusterVolumesRow;

    @UiField
    Row pathEditorRow;

    @UiField
    @Path(value = "configurationMessage")
    Label message;

    @UiField
    @Path(value = "linkGlusterVolume.entity")
    @WithElementId("linkGlusterVolume")
    EntityModelCheckBoxEditor linkGlusterVolumeEditor;

    @UiField(provided = true)
    @Path(value = "glusterVolumes.selectedItem")
    @WithElementId
    ListModelListBoxEditor<GlusterVolumeEntity> glusterVolumesEditor;


    public GlusterStorageView() {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    void initEditors() {
        linkGlusterVolumeEditor =  new EntityModelCheckBoxEditor(Align.RIGHT);
        glusterVolumesEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<GlusterVolumeEntity>() {
            @Override
            protected String renderNullSafe(GlusterVolumeEntity glusterVolume) {
                if (glusterVolume == null) {
                    return ""; //$NON-NLS-1$
                } else {
                    if (glusterVolume.getBricks().isEmpty()) {
                        return glusterVolume.getName();
                    }
                    GlusterBrickEntity brick = glusterVolume.getBricks().get(0);
                    if (brick == null) {
                        return glusterVolume.getName();
                    }
                    String server = brick.getNetworkId() != null && StringHelper.isNotNullOrEmpty(brick.getNetworkAddress()) ? brick.getNetworkAddress() : brick.getServerName();
                    return server + ":/" + glusterVolume.getName(); //$NON-NLS-1$
                }
            }
        });
    }

    @Override
    public void edit(GlusterStorageModel object) {

        final GlusterStorageModel glusterStorageModel = object;
        driver.edit(object);
        glusterVolumesEditor.asEditor().setValue(object.getGlusterVolumes().getSelectedItem());
        pathExampleLabel.setVisible(object.getPath().getIsAvailable() && object.getPath().getIsChangable());
        glusterStorageModel.getLinkGlusterVolume().getEntityChangedEvent().addListener((ev, sender, args) -> {
            // Editor, needs the example
            boolean showEditor = !glusterStorageModel.getLinkGlusterVolume().getEntity();
            pathEditorRow.setVisible(showEditor);
            pathExampleLabel.setVisible(showEditor);

            // List box, shouldn't have an example since you can only select one.
            glusterVolumesRow.setVisible(!showEditor);
        });

    }

    @Override
    public GlusterStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }
}
