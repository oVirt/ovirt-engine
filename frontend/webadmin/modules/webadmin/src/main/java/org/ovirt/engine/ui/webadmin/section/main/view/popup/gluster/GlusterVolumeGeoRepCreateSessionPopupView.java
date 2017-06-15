package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.gluster.GlusterVolumeGeoRepCreateModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.GlusterVolumeGeoRepCreateSessionPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class GlusterVolumeGeoRepCreateSessionPopupView extends AbstractModelBoundPopupView<GlusterVolumeGeoRepCreateModel> implements GlusterVolumeGeoRepCreateSessionPopupPresenterWidget.ViewDef{

    interface Driver extends UiCommonEditorDriver<GlusterVolumeGeoRepCreateModel, GlusterVolumeGeoRepCreateSessionPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, GlusterVolumeGeoRepCreateSessionPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterVolumeGeoRepCreateSessionPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "showEligibleVolumes.entity")
    @WithElementId
    EntityModelCheckBoxEditor showEligibleVolumes;

    @UiField(provided = true)
    @Path(value = "slaveClusters.selectedItem")
    @WithElementId
    ListModelTypeAheadListBoxEditor<String> slaveClusterEditor;

    @UiField(provided = true)
    @Path(value = "slaveHosts.selectedItem")
    @WithElementId
    ListModelTypeAheadListBoxEditor<Pair<String, Guid>> slaveHostIpEditor;

    @UiField(provided = true)
    @Path(value = "slaveVolumes.selectedItem")
    @WithElementId
    ListModelTypeAheadListBoxEditor<GlusterVolumeEntity> slaveVolumeEditor;

    @UiField
    @Path(value = "slaveUserName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor slaveUserName;

    @UiField
    @Path(value = "slaveUserGroupName.entity")
    @WithElementId
    StringEntityModelTextBoxEditor slaveUserGroupNameEditor;

    @UiField(provided = true)
    @Path(value = "startSession.entity")
    @WithElementId
    EntityModelCheckBoxEditor startSessionEditor;

    @UiField
    @Ignore
    @WithElementId
    Alert suggestedConfigViolations;

    @UiField
    @Ignore
    @WithElementId
    Alert message;

    private final ApplicationConstants constants;

    private final CommonApplicationTemplates templates;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public GlusterVolumeGeoRepCreateSessionPopupView(EventBus eventBus,
            ApplicationConstants constants,
            CommonApplicationTemplates templates) {
        super(eventBus);
        this.constants = constants;
        this.templates = templates;
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
        setVisibilities();
    }

    private void setVisibilities() {
        suggestedConfigViolations.setVisible(false);
    }

    private void initEditors() {
        showEligibleVolumes = new EntityModelCheckBoxEditor(Align.RIGHT);

        startSessionEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        slaveClusterEditor = new ListModelTypeAheadListBoxEditor<>(new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<String>() {

            @Override
            public String getReplacementStringNullSafe(String data) {
                return data;
            }

            @Override
            public String getDisplayStringNullSafe(String data) {
                return templates.typeAheadNameDescription(data == null ? constants.empty() : data, constants.empty()).asString();
            }
        });

        slaveHostIpEditor = new ListModelTypeAheadListBoxEditor<>(new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<Pair<String, Guid>>() {

            @Override
            public String getReplacementStringNullSafe(Pair<String, Guid> data) {
                return data.getFirst();
            }

            @Override
            public String getDisplayStringNullSafe(Pair<String, Guid> data) {
                return templates.typeAheadNameDescription(data == null ? constants.empty() : data.getFirst(), constants.empty()).asString();
            }
        });

        slaveVolumeEditor = new ListModelTypeAheadListBoxEditor<>(new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<GlusterVolumeEntity>() {
            @Override
            public String getReplacementStringNullSafe(GlusterVolumeEntity data) {
                return data.getName();
            }

            @Override
            public String getDisplayStringNullSafe(GlusterVolumeEntity data) {
                return templates.typeAheadNameDescription(data.getName() == null ? constants.empty() : data.getName(), data.getClusterName() == null ? constants.empty() : data.getClusterName()).asString();
            }
        });
    }

    @Override
    public void edit(final GlusterVolumeGeoRepCreateModel object) {
        driver.edit(object);
    }

    @Override
    public void setFailureMessage(String failureMessage) {
        boolean failureMessageVisible = failureMessage != null;
        if(failureMessageVisible) {
            message.setText(failureMessage);
        }
        message.setVisible(failureMessageVisible);
    }

    @Override
    public void setSuggestedConfigViolations(String recommendationViolations) {
        boolean recommendationViolationsVisible = recommendationViolations != null;
        if(recommendationViolationsVisible) {
            suggestedConfigViolations.setText(recommendationViolations);
        }
        suggestedConfigViolations.setVisible(recommendationViolationsVisible);
    }

    @Override
    public GlusterVolumeGeoRepCreateModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
