package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class VmMakeTemplatePopupWidget extends AbstractModelBoundPopupWidget<UnitVmModel> {

    interface Driver extends SimpleBeanEditorDriver<UnitVmModel, VmMakeTemplatePopupWidget> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmMakeTemplatePopupWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmMakeTemplatePopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    @WithElementId("description")
    EntityModelTextBoxEditor descriptionEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField(provided = true)
    @Ignore
    @WithElementId("disksAllocation")
    DisksAllocationView disksAllocationView;

    @UiField(provided = true)
    @Path(value = "quota.selectedItem")
    @WithElementId("quota")
    ListModelListBoxEditor<Object> quotaEditor;

    @UiField(provided = true)
    @Path(value = "isTemplatePrivate.entity")
    @WithElementId("isTemplatePrivate")
    EntityModelCheckBoxEditor isTemplatePrivateEditor;

    @UiField
    Label message;

    @UiField
    @Ignore
    Label disksAllocationLabel;

    public VmMakeTemplatePopupWidget(CommonApplicationConstants constants) {
        initListBoxEditors();
        initCheckBoxEditors();
        disksAllocationView = new DisksAllocationView(constants);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });

        quotaEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Quota) object).getQuotaName();
            }
        });
    }

    void initCheckBoxEditors() {
        isTemplatePrivateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(CommonApplicationConstants constants) {
        nameEditor.setLabel(constants.makeTemplatePopupNameLabel());
        descriptionEditor.setLabel(constants.makeTemplatePopupDescriptionLabel());
        clusterEditor.setLabel(constants.makeTemplateClusterLabel());
        quotaEditor.setLabel(constants.makeTemplateQuotaLabel());
        isTemplatePrivateEditor.setLabel(constants.makeTemplateIsTemplatePrivateEditorLabel());
        disksAllocationLabel.setText(constants.disksAllocation());
    }

    @Override
    public void edit(final UnitVmModel model) {
        Driver.driver.edit(model);

        model.getStorageDomain().getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                addDiskAllocation(model);
            }
        });

        model.getStorageDomain().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                boolean isDisksAllocationEnabled = model.getDisks() != null && !model.getDisks().isEmpty();
                disksAllocationView.setEnabled(isDisksAllocationEnabled);
                disksAllocationLabel.getElement().getStyle().setColor(isDisksAllocationEnabled ? "black" : "grey"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
    }

    private void addDiskAllocation(UnitVmModel model) {
        disksAllocationView.edit(model.getDisksAllocationModel());
        model.getDisksAllocationModel().getStorageDomain().setItems(model.getStorageDomain().getItems());
        model.getDisksAllocationModel().setDisks(model.getDisks());
    }

    @Override
    public UnitVmModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

}
