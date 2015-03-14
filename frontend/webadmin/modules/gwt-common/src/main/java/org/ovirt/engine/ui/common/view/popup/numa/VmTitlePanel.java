package org.ovirt.engine.ui.common.view.popup.numa;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VmTitlePanel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, VmTitlePanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField(provided=true)
    final CommonApplicationResources resources = AssetProvider.getResources();

    @UiField
    Image vmStatus;

    @UiField
    Label vmName;

    @UiField
    Label nodeCountLabel;

    @Inject
    public VmTitlePanel() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    public void initWidget(SafeHtml vmNameString, int nodeCount, VMStatus status) {
        vmName.setText(vmNameString.asString());
        nodeCountLabel.setText(String.valueOf(nodeCount));
        setStatusIcon(status);
    }

    private void setStatusIcon(VMStatus status) {
        if (VMStatus.Up.equals(status)) {
            vmStatus.setResource(resources.upImage());
        } else if (VMStatus.Down.equals(status)) {
            vmStatus.setResource(resources.downImage());
        } else {
            //Unknown status
            vmStatus.setResource(resources.questionMarkImage());
        }
    }
}
