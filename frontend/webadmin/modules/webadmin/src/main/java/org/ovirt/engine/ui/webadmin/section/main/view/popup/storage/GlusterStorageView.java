package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
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
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
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

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    WidgetStyle style;

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
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
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
                    String server = brick.getNetworkId() != null && !StringUtils.isEmpty(brick.getNetworkAddress()) ? brick.getNetworkAddress() : brick.getServerName();
                    return server + ":/" + glusterVolume.getName(); //$NON-NLS-1$
                }
            }
        });
    }

    void addStyles() {
        pathEditor.addContentWidgetContainerStyleName(style.pathEditorContent());
        vfsTypeEditor.addContentWidgetContainerStyleName(style.vfsTypeTextBoxEditor());
        mountOptionsEditor.addContentWidgetContainerStyleName(style.mountOptionsTextBoxEditor());
        glusterVolumesEditor.addContentWidgetContainerStyleName(style.glusterVolumesEditor());
        linkGlusterVolumeEditor.addContentWidgetContainerStyleName(style.linkGlusterVolumeEditor());
    }

    void localize() {
        pathEditor.setLabel(constants.storagePopupPosixPathLabel());
        pathExampleLabel.setText(constants.storagePopupGlusterPathExampleLabel());
        vfsTypeEditor.setLabel(constants.storagePopupVfsTypeLabel());
        mountOptionsEditor.setLabel(constants.storagePopupMountOptionsLabel());
        linkGlusterVolumeEditor.setLabel(constants.storagePopupLinkGlusterVolumeLabel());
        glusterVolumesEditor.setLabel(constants.glusterVolume());
    }

    @Override
    public void edit(GlusterStorageModel object) {

        final GlusterStorageModel glusterStorageModel = object;
        driver.edit(object);
        glusterVolumesEditor.asEditor().setValue(object.getGlusterVolumes().getSelectedItem());
        glusterVolumesEditor.setVisible(false);
        pathExampleLabel.setVisible(object.getPath().getIsAvailable() && object.getPath().getIsChangable());
        glusterStorageModel.getLinkGlusterVolume().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (glusterStorageModel.getLinkGlusterVolume().getEntity()) {
                    glusterVolumesEditor.setVisible(true);
                    pathEditor.setVisible(false);
                } else {
                    glusterVolumesEditor.setVisible(false);
                    pathEditor.setVisible(true);
                }
            }
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

    interface WidgetStyle extends CssResource {

        String pathEditorContent();

        String vfsTypeTextBoxEditor();

        String mountOptionsTextBoxEditor();

        String glusterVolumesEditor();

        String linkGlusterVolumeEditor();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }
}
