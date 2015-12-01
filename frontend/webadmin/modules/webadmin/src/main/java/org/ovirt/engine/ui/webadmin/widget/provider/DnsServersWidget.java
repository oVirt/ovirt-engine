package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.ui.common.widget.ScrollableAddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class DnsServersWidget extends ScrollableAddRemoveRowWidget<ListModel<EntityModel<String>>, EntityModel<String>, DnsServerEditor> {

    public interface WidgetUiBinder extends UiBinder<Widget, DnsServersWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public DnsServersWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected DnsServerEditor createWidget(EntityModel<String> value) {
        DnsServerEditor widget = new DnsServerEditor();
        widget.edit(value);
        return widget;
    }

    @Override
    protected EntityModel<String> createGhostValue() {
        EntityModel<String> value = new EntityModel<>();
        value.setEntity(""); //$NON-NLS-1$
        return value;
    }

    @Override
    protected boolean isGhost(EntityModel<String> value) {
        return StringUtils.isEmpty(value.getEntity());
    }
}
