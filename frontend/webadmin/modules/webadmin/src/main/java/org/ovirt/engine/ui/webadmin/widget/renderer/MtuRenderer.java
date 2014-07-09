package org.ovirt.engine.ui.webadmin.widget.renderer;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.AbstractRenderer;

public class MtuRenderer extends AbstractRenderer<Integer> {

    private static int defaultMtu =
            (Integer) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.DefaultMtu);

    private final ApplicationMessages messages = GWT.create(ApplicationMessages.class);

    @Override
    public String render(Integer mtu) {
        return mtu == 0 ? messages.defaultMtu(defaultMtu) : mtu.toString();
    }

}
