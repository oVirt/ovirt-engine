package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.text.shared.Parser;
import java.text.ParseException;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ToStringEntityModelRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

public class PoolEditPopupWidget extends PoolNewPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<PoolEditPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public PoolEditPopupWidget(CommonApplicationConstants constants,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationTemplates applicationTemplates,
            EventBus eventBus) {
        super(constants, resources, messages, applicationTemplates, eventBus);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (object.getProgress() == null) {
                    disableAllTabs();
                    enableEditPoolFields(object);
                }
            }

        });
    }

    @Override
    protected void createNumOfDesktopEditors() {
        incraseNumOfVmsEditor = new IntegerEntityModelTextBoxOnlyEditor();

        numOfVmsEditor = new EntityModelTextBoxEditor<Integer>(new ToStringEntityModelRenderer<Integer>(), new Parser<Integer>() {

            @Override
            public Integer parse(CharSequence text) throws ParseException {
                // forwards to the currently active editor
                return incraseNumOfVmsEditor.asEditor().getValue();
            }

        });
    }

    @Override
    public void focusInput() {
        descriptionEditor.setFocus(true);
    }

    private void enableEditPoolFields(UnitVmModel model) {
        descriptionEditor.setEnabled(true);
        commentEditor.setEnabled(true);
        prestartedVmsEditor.setEnabled(true);

        editPrestartedVmsEditor.setEnabled(true);
        incraseNumOfVmsEditor.setEnabled(true);
        editMaxAssignedVmsPerUserEditor.setEnabled(true);

        spiceProxyEditor.setEnabled(model.getSpiceProxyEnabled().getEntity());
    }

    @Override
    protected void applyStyles() {
        super.applyStyles();

        // In 'ja' locale the text of prestarted vms is too long for 230px.
        // Changed all the right column widgets width to 250px.
        dataCenterWithClusterEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        vmTypeEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        quotaEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        instanceTypesEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        descriptionEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        templateEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        memSizeEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        detachableMemSizeEditor.addContentWrapperStypeName(style.generalTabExtendedRightWidgetWrapperWidth());
        totalvCPUsEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        totalvCPUsEditorWithInfoIcon.getContentWidget().addContentWrapperStypeName(style.generalTabExtendedRightWidgetWrapperWidth());
        corePerSocketEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        corePerSocketEditorWithDetachable.addContentWrapperStypeName(style.generalTabExtendedRightWidgetWrapperWidth());
        numOfSocketsEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        numOfSocketsEditorWithDetachable.addContentWrapperStypeName(style.generalTabExtendedRightWidgetWrapperWidth());
        oSTypeEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        isStatelessEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        isRunAndPauseEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        isDeleteProtectedEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        editIncreaseVmsPanel.addStyleName(style.generalTabExtendedRightWidgetWidth());
        editPrestartedVmsPanel.addStyleName(style.generalTabExtendedRightWidgetWidth());
        nameEditor.addStyleName(style.generalTabExtendedRightWidgetWidth());
        nameEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                update(spiceProxyEditor, simpleField().visibleInAdvancedModeOnly()).
                update(spiceProxyEnabledCheckboxWithInfoIcon, simpleField().visibleInAdvancedModeOnly()).
                update(spiceProxyOverrideEnabledEditor, simpleField().visibleInAdvancedModeOnly()).
                update(numOfVmsEditor, hiddenField()).
                update(newPoolEditVmsPanel, hiddenField()).
                update(newPoolEditMaxAssignedVmsPerUserPanel, hiddenField()).
                update(editPoolEditVmsPanel, simpleField()).
                update(editPoolIncraseNumOfVmsPanel, simpleField()).
                update(editPoolEditMaxAssignedVmsPerUserPanel, simpleField()).
                update(templateVersionNameEditor, hiddenField());
    }

}
