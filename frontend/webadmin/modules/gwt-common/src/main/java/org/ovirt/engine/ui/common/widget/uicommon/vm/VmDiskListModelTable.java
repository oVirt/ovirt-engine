package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ImageUiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmDiskListModel;

import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.shared.EventBus;

public class VmDiskListModelTable extends BaseVmDiskListModelTable<VmDiskListModel> {

    private final CommonApplicationResources resources;
    private final CommonApplicationConstants constants;

    private final boolean showMoveButton;

    private ImageUiCommandButtonDefinition<DiskImage> plugButtonDefinition;
    private ImageUiCommandButtonDefinition<DiskImage> unPlugButtonDefinition;

    public VmDiskListModelTable(
            SearchableTableModelProvider<DiskImage, VmDiskListModel> modelProvider,
            EventBus eventBus, ClientStorage clientStorage,
            CommonApplicationResources resources,
            CommonApplicationConstants constants,
            boolean showMoveButton) {
        super(modelProvider, eventBus, clientStorage);
        this.resources = resources;
        this.constants = constants;
        this.showMoveButton = showMoveButton;
    }

    @Override
    public void initTable() {
        super.initTable();

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "New") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        plugButtonDefinition = new ImageUiCommandButtonDefinition<DiskImage>(getEventBus(), "Activate",
                resources.upImage(), resources.upDisabledImage(), true, false) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPlugCommand();
            }

            @Override
            public String getCustomToolTip() {
                if (!getModel().isVmDown() && getModel().isHotPlugAvailable()
                        && !getModel().getIsDiskHotPlugSupported()) {
                    return constants.diskHotPlugNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        };
        getTable().addActionButton(plugButtonDefinition);

        unPlugButtonDefinition = new ImageUiCommandButtonDefinition<DiskImage>(getEventBus(), "Deactivate",
                resources.downImage(), resources.downDisabledImage(), true, false) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUnPlugCommand();
            }

            @Override
            public String getCustomToolTip() {
                if (!getModel().isVmDown() && getModel().isHotPlugAvailable()
                        && !getModel().getIsDiskHotPlugSupported()) {
                    return constants.diskHotPlugNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        };
        getTable().addActionButton(unPlugButtonDefinition);

        attachActivationListenersForModel();

        if (showMoveButton) {
            getTable().addActionButton(new UiCommandButtonDefinition<DiskImage>(getEventBus(), "Move") {
                @Override
                protected UICommand resolveCommand() {
                    return getModel().getMoveCommand();
                }
            });
        }
    }

    protected void attachActivationListenersForModel() {
        getModel().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                if ("IsDiskHotPlugAvailable".equals(changedArgs.PropertyName)) {
                    InitializeEvent.fire(plugButtonDefinition);
                    InitializeEvent.fire(unPlugButtonDefinition);
                }
            }
        });

        getModel().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                InitializeEvent.fire(plugButtonDefinition);
                InitializeEvent.fire(unPlugButtonDefinition);
            }
        });
    }

}
