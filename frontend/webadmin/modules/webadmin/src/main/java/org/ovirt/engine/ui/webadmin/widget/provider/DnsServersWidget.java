package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class DnsServersWidget extends AddRemoveRowWidget<ListModel<EntityModel<String>>, EntityModel<String>, DnsServerEditor> {

    public interface WidgetUiBinder extends UiBinder<Widget, DnsServersWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public DnsServersWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    private double width = 150;

    @Override
    protected DnsServerEditor createWidget(EntityModel<String> value) {
        DnsServerEditor widget = new DnsServerEditor();
        if (usePatternFly) {
            widget.setUsePatternFly(true);
            widget.hideLabel();
            widget.getElement().getStyle().setWidth(width, Unit.PX);
        }
        widget.edit(value);
        return widget;
    }

    @Override
    protected EntityModel<String> createGhostValue() {
        EntityModel<String> value = new EntityModel<>();
        value.setEntity("");
        return value;
    }

    @Override
    protected boolean isGhost(EntityModel<String> value) {
        return StringUtils.isEmpty(value.getEntity());
    }

    public void setDnsServerWidth(double value) {
        this.width = value;
    }

}
