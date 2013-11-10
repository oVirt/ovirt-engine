package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public class RunOnceVmInitWidget extends VmInitWidget {

    public interface CustomStyleResource extends VmInitWidget.Resources {

        interface Style extends BasicStyle {
        }

        @Override
        @ClientBundle.Source({ VmInitWidget.BasicStyle.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/RunOnceVmInitStyle.css" })
        Style createStyle();
    }

    public RunOnceVmInitWidget() {
        super(((CustomStyleResource) GWT.create(CustomStyleResource.class)).createStyle());
    }
}


