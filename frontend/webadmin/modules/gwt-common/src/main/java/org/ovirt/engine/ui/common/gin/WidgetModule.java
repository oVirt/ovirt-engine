package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.view.popup.numa.AssignedVNumaNodesPanel;
import org.ovirt.engine.ui.common.view.popup.numa.CpuSummaryPanel;
import org.ovirt.engine.ui.common.view.popup.numa.DragTargetScrollPanel;
import org.ovirt.engine.ui.common.view.popup.numa.DraggableVirtualNumaPanel;
import org.ovirt.engine.ui.common.view.popup.numa.HostSummaryContentPanel;
import org.ovirt.engine.ui.common.view.popup.numa.MemorySummaryPanel;
import org.ovirt.engine.ui.common.view.popup.numa.NumaPanel;
import org.ovirt.engine.ui.common.view.popup.numa.SocketPanel;
import org.ovirt.engine.ui.common.view.popup.numa.VirtualNumaPanel;
import org.ovirt.engine.ui.common.view.popup.numa.VirtualNumaPanelDetails;
import org.ovirt.engine.ui.common.view.popup.numa.VmTitlePanel;

import com.google.gwt.inject.client.AbstractGinModule;

public class WidgetModule extends AbstractGinModule {

    @Override
    protected void configure() {
        //NUMA popup widgets.
        bind(CpuSummaryPanel.class);
        bind(MemorySummaryPanel.class);
        bind(NumaPanel.class);
        bind(SocketPanel.class);
        bind(AssignedVNumaNodesPanel.class);
        bind(DraggableVirtualNumaPanel.class);
        bind(DragTargetScrollPanel.class);
        bind(VmTitlePanel.class);
        bind(HostSummaryContentPanel.class);
        bind(VirtualNumaPanel.class);
        bind(VirtualNumaPanelDetails.class);
    }

}
