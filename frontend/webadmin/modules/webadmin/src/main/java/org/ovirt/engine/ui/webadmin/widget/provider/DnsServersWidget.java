package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.dnsconfiguration.NameServerModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class DnsServersWidget extends AddRemoveRowWidget<ListModel<NameServerModel>, NameServerModel, DnsServerEditor> {

    public interface WidgetUiBinder extends UiBinder<Widget, DnsServersWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public DnsServersWidget() {
        super(BusinessEntitiesDefinitions.MAX_SUPPORTED_DNS_CONFIGURATIONS);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    private double width = 150;

    @Override
    protected DnsServerEditor createWidget(NameServerModel value) {
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
    protected NameServerModel createGhostValue() {
        return new NameServerModel();
    }

    @Override
    protected boolean isGhost(NameServerModel value) {
        return StringHelper.isNullOrEmpty(value.getEntity());
    }

    public void setDnsServerWidth(double value) {
        this.width = value;
    }

}
