package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterHookStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerHook;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelRadioButtonEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelEnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.GlusterHookResolveConflictsModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookResolveConflictsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

public class GlusterHookResolveConflictsPopupView extends AbstractModelBoundPopupView<GlusterHookResolveConflictsModel> implements GlusterHookResolveConflictsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<GlusterHookResolveConflictsModel, GlusterHookResolveConflictsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, GlusterHookResolveConflictsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterHookResolveConflictsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Ignore
    @WithElementId
    Label conflictReasonsLabel;

    @UiField
    @Ignore
    @WithElementId
    Label conflictReasonsContentDiffLabel;

    @UiField
    @Ignore
    @WithElementId
    Label conflictReasonsStatusDiffLabel;

    @UiField
    @Ignore
    @WithElementId
    Label conflictReasonsMissingHookLabel;

    @UiField
    @Ignore
    @WithElementId
    Label contentSourcesLabel;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> contentSourcesTable;

    @UiField
    @Path(value = "contentModel.content.entity")
    @WithElementId
    StringEntityModelTextAreaLabelEditor contentEditor;

    @UiField
    @Path(value = "contentModel.md5Checksum.entity")
    @WithElementId
    StringEntityModelLabelEditor checksumEditor;

    @UiField(provided = true)
    @Path(value = "contentModel.status.entity")
    @WithElementId
    EntityModelLabelEditor<GlusterHookStatus> statusEditor;

    @UiField
    @Ignore
    Label resolveHeaderLabel;

    @UiField
    @Ignore
    VerticalPanel resolveContentConflictPanel;

    @UiField(provided = true)
    @Path("resolveContentConflict.entity")
    @WithElementId
    EntityModelCheckBoxEditor resolveContentConflict;

    @UiField(provided = true)
    @Path(value = "serverHooksList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<GlusterServerHook> useContentSourceEditor;

    @UiField
    @Ignore
    Label useContentSourceWarning;

    @UiField
    @Ignore
    VerticalPanel resolveStatusConflictPanel;

    @UiField(provided = true)
    @Path("resolveStatusConflict.entity")
    @WithElementId
    EntityModelCheckBoxEditor resolveStatusConflict;

    @UiField
    @Ignore
    Label hookStatusLabel;

    @UiField(provided = true)
    @Path("resolveStatusConflictEnable.entity")
    @WithElementId
    EntityModelRadioButtonEditor resolveStatusConflictEnable;

    @UiField(provided = true)
    @Path("resolveStatusConflictDisable.entity")
    @WithElementId
    EntityModelRadioButtonEditor resolveStatusConflictDisable;

    @UiField
    @Ignore
    VerticalPanel resolveMissingConflictPanel;

    @UiField(provided = true)
    @Path("resolveMissingConflict.entity")
    @WithElementId
    EntityModelCheckBoxEditor resolveMissingConflict;

    @UiField(provided = true)
    @Path("resolveMissingConflictCopy.entity")
    @WithElementId
    EntityModelRadioButtonEditor resolveMissingConflictCopyEditor;

    @UiField(provided = true)
    @Path("resolveMissingConflictRemove.entity")
    @WithElementId
    EntityModelRadioButtonEditor resolveMissingConflictRemoveEditor;

    @UiField
    @Ignore
    Label messageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public GlusterHookResolveConflictsPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        localize();
        initTableColumns();
        driver.initialize(this);
    }

    private void initEditors() {
        contentSourcesTable = new EntityModelCellTable<>(false, true);

        statusEditor = new EntityModelLabelEditor<>(new EnumRenderer<GlusterHookStatus>());

        resolveContentConflict = new EntityModelCheckBoxEditor(Align.RIGHT);
        useContentSourceEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<GlusterServerHook>() {
            @Override
            protected String renderNullSafe(GlusterServerHook hook) {
                return hook.getServerName();
            }
        });

        resolveStatusConflict = new EntityModelCheckBoxEditor(Align.RIGHT);
        resolveStatusConflictEnable = new EntityModelRadioButtonEditor("status", Align.RIGHT); //$NON-NLS-1$
        resolveStatusConflictDisable = new EntityModelRadioButtonEditor("status", Align.RIGHT); //$NON-NLS-1$

        resolveMissingConflict = new EntityModelCheckBoxEditor(Align.RIGHT);
        resolveMissingConflictCopyEditor = new EntityModelRadioButtonEditor("missing_hook", Align.RIGHT); //$NON-NLS-1$
        resolveMissingConflictRemoveEditor = new EntityModelRadioButtonEditor("missing_hook", Align.RIGHT); //$NON-NLS-1$
    }

    private void addStyles() {
        contentEditor.addContentWidgetContainerStyleName(style.contentViewWidget());
        checksumEditor.addContentWidgetContainerStyleName(style.contentViewWidget());
        statusEditor.addContentWidgetContainerStyleName(style.contentViewWidget());
    }

    private void localize() {
        conflictReasonsLabel.setText(constants.conflictReasonsGlusterHook());
        conflictReasonsContentDiffLabel.setText(constants.conflictReasonContentGlusterHook());
        conflictReasonsStatusDiffLabel.setText(constants.conflictReasonStatusGlusterHook());
        conflictReasonsMissingHookLabel.setText(constants.conflictReasonMissingGlusterHook());

        contentSourcesLabel.setText(constants.contentSourcesGlusterHook());
        contentEditor.setLabel(constants.contentGlusterHook());
        checksumEditor.setLabel(constants.checksumGlusterHook());
        statusEditor.setLabel(constants.statusGlusterHook());

        resolveHeaderLabel.setText(constants.resolveActionsGlusterHook());

        resolveContentConflict.setLabel(constants.resolveContentConflictGlusterHook());
        useContentSourceEditor.setLabel(constants.useContentSourceGlusterHook());
        useContentSourceWarning.setText(constants.useContentSourceWarningGlusterHook());

        resolveStatusConflict.setLabel(constants.resolveStatusConflictGlusterHook());
        hookStatusLabel.setText(constants.statusGlusterHook());
        resolveStatusConflictEnable.setLabel(constants.statusEnableGlusterHook());
        resolveStatusConflictDisable.setLabel(constants.statusDisableGlusterHook());

        resolveMissingConflict.setLabel(constants.resolveMissingConflictGlusterHook());
        resolveMissingConflictCopyEditor.setLabel(constants.resolveMissingConflictCopyGlusterHook());
        resolveMissingConflictRemoveEditor.setLabel(constants.resolveMissingConflictRemoveGlusterHook());
    }

    private void initTableColumns() {
        contentSourcesTable.addColumn(new AbstractEntityModelTextColumn<GlusterServerHook>() {
            @Override
            public String getText(GlusterServerHook entity) {
                return entity.getServerName();
            }
        }, constants.sourceGlusterHook());

        contentSourcesTable.addColumn(new AbstractEntityModelEnumColumn<GlusterServerHook, GlusterHookStatus>() {
            @Override
            protected GlusterHookStatus getEnum(GlusterServerHook entity) {
                return entity.getStatus();
            }
        }, constants.statusGlusterHook());
    }

    @Override
    public void edit(GlusterHookResolveConflictsModel object) {
        driver.edit(object);

        contentSourcesTable.asEditor().edit(object.getHookSources());

        conflictReasonsContentDiffLabel.setVisible(object.getGlusterHookEntity().isContentConflict());
        conflictReasonsStatusDiffLabel.setVisible(object.getGlusterHookEntity().isStatusConflict());
        conflictReasonsMissingHookLabel.setVisible(object.getGlusterHookEntity().isMissingHookConflict());

        resolveContentConflictPanel.setVisible(object.getGlusterHookEntity().isContentConflict());
        resolveStatusConflictPanel.setVisible(object.getGlusterHookEntity().isStatusConflict());
        resolveMissingConflictPanel.setVisible(object.getGlusterHookEntity().isMissingHookConflict());
    }

    @Override
    public GlusterHookResolveConflictsModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    interface WidgetStyle extends CssResource {
        String contentViewWidget();
    }

}
