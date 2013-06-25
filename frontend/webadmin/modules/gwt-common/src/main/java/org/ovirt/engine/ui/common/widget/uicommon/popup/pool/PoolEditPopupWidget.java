package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.hiddenField;
import static org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfig.simpleField;

import java.text.ParseException;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRenderer;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PopupWidgetConfigMap;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.Parser;

public class PoolEditPopupWidget extends PoolNewPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<PoolEditPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public PoolEditPopupWidget(CommonApplicationConstants constants,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationTemplates applicationTemplates) {
        super(constants, resources, messages, applicationTemplates);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (object.getProgress() == null) {
                    disableAllTabs();
                    enableEditPoolFields();
                }
            }

        });
    }

    @Override
    protected void createNumOfDesktopEditors() {
        incraseNumOfVmsEditor = new EntityModelTextBoxOnlyEditor();

        numOfVmsEditor = new EntityModelTextBoxEditor(new EntityModelRenderer(), new Parser<Object>() {

            @Override
            public Object parse(CharSequence text) throws ParseException {
                // forwards to the currently active editor
                return incraseNumOfVmsEditor.asEditor().getValue();
            }

        });
    }

    @Override
    public void focusInput() {
        descriptionEditor.setFocus(true);
    }

    private void enableEditPoolFields() {
        descriptionEditor.setEnabled(true);
        prestartedVmsEditor.setEnabled(true);

        editPrestartedVmsEditor.setEnabled(true);
        incraseNumOfVmsEditor.setEnabled(true);
        editMaxAssignedVmsPerUserEditor.setEnabled(true);
    }

    @Override
    protected void applyStyles() {
        super.applyStyles();

        // In 'ja' locale the text of prestarted vms is too long for 230px.
        // Changed all the right column widgets width to 250px.
        dataCenterWithClusterEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        vmTypeEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        quotaEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        descriptionEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        templateEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        memSizeEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        totalvCPUsEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        corePerSocketEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        numOfSocketsEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        oSTypeEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        isStatelessEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        isRunAndPauseEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        isDeleteProtectedEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());
        editIncreaseVmsPanel.addStyleName(style.generalTabExtendedRightWidgetWidth());
        editPrestartedVmsPanel.addStyleName(style.generalTabExtendedRightWidgetWidth());
        nameEditor.addStyleName(style.generalTabExtendedRightWidgetWidth());
        nameEditor.addContentWidgetStyleName(style.generalTabExtendedRightWidgetWidth());

    }

    private void disableAllTabs() {
        generalTab.disableContent();
        poolTab.disableContent();
        initialRunTab.disableContent();
        consoleTab.disableContent();
        hostTab.disableContent();
        highAvailabilityTab.disableContent();
        resourceAllocationTab.disableContent();
        bootOptionsTab.disableContent();
        customPropertiesTab.disableContent();
        systemTab.disableContent();
        oSTypeEditor.setEnabled(false);
    }

    @Override
    protected PopupWidgetConfigMap createWidgetConfiguration() {
        return super.createWidgetConfiguration().
                update(numOfVmsEditor, hiddenField()).
                update(newPoolEditVmsPanel, hiddenField()).
                update(newPoolEditMaxAssignedVmsPerUserPanel, hiddenField()).
                update(editPoolEditVmsPanel, simpleField()).
                update(editPoolIncraseNumOfVmsPanel, simpleField()).
                update(editPoolEditMaxAssignedVmsPerUserPanel, simpleField());
    }

}
