package org.ovirt.engine.ui.common.widget.uicommon.disks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DisksContentTypeRadioGroup extends FlowPanel {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final String BUTTON_GROUP_NAME = "diskContentTypeView"; //$NON-NLS-1$

    Map<RadioButton, DiskContentType> buttonToType;

    private final List<DisksContentViewChangeHandler> changeHandlers = new ArrayList<>();

    public interface DisksContentViewChangeHandler {
        /**
         * Called when the selected disks content type changes.
         */
        void disksContentViewChanged(DiskContentType newType);
    }

    public DisksContentTypeRadioGroup() {
        Label label = new Label();
        label.setText(constants.diskContentType() + ":"); //$NON-NLS-1$
        label.addStyleName("disk-content-type-group-label"); //$NON-NLS-1$
        add(label);
        add(getRadioGroupPanel());
   }

    private Widget getRadioGroupPanel() {
        buttonToType = new LinkedHashMap<>();

        RadioButton allButton = new RadioButton(BUTTON_GROUP_NAME); //$NON-NLS-1$
        buttonToType.put(allButton, null);
        allButton.setText(constants.allDisksLabel());

        for (DiskContentType contentType : DiskContentType.values()) {
            RadioButton button = new RadioButton(BUTTON_GROUP_NAME); //$NON-NLS-1$
            buttonToType.put(button, contentType);
        }

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setDataToggle(Toggle.BUTTONS);
        buttonToType.entrySet().forEach(e -> {
            buttonGroup.add(e.getKey());
            e.getKey().addClickHandler(event -> fireChangeHandlers(e.getValue()));
        });

        setDiskContentType(null);
        localize();
        buttonGroup.addStyleName("disk-type-buttons-group"); //$NON-NLS-1$
        return buttonGroup;
    }

    public void addChangeHandler(DisksContentViewChangeHandler handler) {
        if (!changeHandlers.contains(handler)) {
            changeHandlers.add(handler);
        }
    }

    private void fireChangeHandlers(DiskContentType type) {
        for (DisksContentViewChangeHandler disksViewChangeHandler : changeHandlers) {
            disksViewChangeHandler.disksContentViewChanged(type);
        }
    }


    void localize() {
        buttonToType.entrySet().stream().filter(e -> e.getValue() != null)
                .forEach(e -> e.getKey().setText(EnumTranslator.getInstance().translate(e.getValue())));
    }

    public DiskContentType getDiskContentType() {
        return buttonToType.entrySet().stream().filter(e -> e.getKey().getValue()).findFirst().get().getValue();
    }

    public void setDiskContentType(DiskContentType diskContentType) {
        buttonToType.entrySet().stream().forEach(e -> e.getKey().setValue(diskContentType == e.getValue()));
    }

}
