package org.ovirt.engine.ui.common.presenter.popup.numa;

import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class NumaVmSelected {
    VNodeModel vNodeModel;
}
