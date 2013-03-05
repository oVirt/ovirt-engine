package org.ovirt.engine.ui.common.widget.uicommon.popup.pool;

import java.text.ParseException;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.editor.EntityModelRenderer;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractVmPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.Parser;

public class PoolNewPopupWidget extends AbstractVmPopupWidget {

    interface ViewIdHandler extends ElementIdHandler<PoolNewPopupWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public PoolNewPopupWidget(CommonApplicationConstants constants, CommonApplicationResources resources, CommonApplicationMessages messages, CommonApplicationTemplates applicationTemplates) {
        super(constants, resources, messages, applicationTemplates);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(final UnitVmModel object) {
        super.edit(object);
        initTabAvailabilityListeners(object);
        isStatelessEditor.setVisible(false);
        poolNameIcon.setVisible(true);

        if (object.getIsNew()) {
            prestartedVmsEditor.setEnabled(false);
            object.getNumOfDesktops().setEntity("1"); //$NON-NLS-1$

            numOfVmsEditor.setVisible(true);
            newPoolEditVmsPanel.setVisible(true);
            editPoolEditVmsPanel.setVisible(false);
            editPoolIncraseNumOfVmsPanel.setVisible(false);
        } else {
            numOfVmsEditor.setVisible(false);
            newPoolEditVmsPanel.setVisible(false);
            editPoolEditVmsPanel.setVisible(true);
            prestartedVmsEditor.setEnabled(true);
            editPoolIncraseNumOfVmsPanel.setVisible(true);
        }
    }

    @Override
    protected void createNumOfDesktopEditors() {
        numOfVmsEditor = new EntityModelTextBoxEditor();
        incraseNumOfVmsEditor = new EntityModelTextBoxOnlyEditor(new EntityModelRenderer(), new Parser<Object>() {

            @Override
            public Object parse(CharSequence text) throws ParseException {
                // forwards to the currently active editor
                return numOfVmsEditor.asEditor().getValue();
            }

        });
    }

    private void initTabAvailabilityListeners(final UnitVmModel pool) {
        // TODO should be handled by the core framework
        pool.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if ("IsPoolTabValid".equals(propName)) { //$NON-NLS-1$
                    poolTab.markAsInvalid(null);
                }
            }
        });

        poolTab.setVisible(true);
    }

}
