package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class IscsiTargetToLunView extends Composite implements HasEditorDriver<SanStorageModelBase> {

    interface Driver extends SimpleBeanEditorDriver<SanStorageModelBase, IscsiTargetToLunView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiTargetToLunView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel targetsListPanel;

    @UiField
    SimplePanel targetsToLunsDiscoverPanel;

    @Ignore
    IscsiDiscoverTargetsView iscsiDiscoverTargetsView;

    SanStorageTargetToLunList sanStorageTargetToLunList;

    double treeCollapsedHeight;
    double treeExpandedHeight;
    boolean hideLeaf;
    boolean multiSelection;

    public IscsiTargetToLunView(double treeCollapsedHeight, double treeExpandedHeight) {
        this(treeCollapsedHeight, treeExpandedHeight, false, false);
    }

    public IscsiTargetToLunView(double treeCollapsedHeight, double treeExpandedHeight,
            boolean hideLeaf, boolean multiSelection) {
        this.treeCollapsedHeight = treeCollapsedHeight;
        this.treeExpandedHeight = treeExpandedHeight;
        this.hideLeaf = hideLeaf;
        this.multiSelection = multiSelection;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    @Override
    public void edit(final SanStorageModelBase object) {
        Driver.driver.edit(object);

        initLists(object);

        // Set DiscoverTargetsCommand as default (for iscsiDiscoverTargetsView)
        object.getDiscoverTargetsCommand().setIsDefault(true);
        object.getCommands().add(object.getDiscoverTargetsCommand());

        // Add event handlers
        object.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;
                if (propName.equals("ProposeDiscoverTargets")) { //$NON-NLS-1$
                    setProposeDiscover(object.getProposeDiscoverTargets());
                }
            }
        });

        // Edit sub view
        iscsiDiscoverTargetsView.edit(object);

        // Set discover panel's style by 'ProposeDiscover' flag
        setProposeDiscover(object.getProposeDiscoverTargets());
    }

    private void setProposeDiscover(boolean proposeDiscover) {
        // Update discover panel visibility according to propose discover flag
        if (sanStorageTargetToLunList != null) {
            sanStorageTargetToLunList.setTreeContainerHeight(proposeDiscover ? treeCollapsedHeight : treeExpandedHeight);
        }
    }

    void initLists(SanStorageModelBase object) {
        // Create discover panel and storage lists
        iscsiDiscoverTargetsView = new IscsiDiscoverTargetsView();
        sanStorageTargetToLunList = new SanStorageTargetToLunList(object, hideLeaf, multiSelection);

        // Add view widgets to panel
        targetsToLunsDiscoverPanel.add(iscsiDiscoverTargetsView);
        targetsListPanel.add(sanStorageTargetToLunList);

        // Disable Discover widget in view-only mode
        iscsiDiscoverTargetsView.setEnabled(object.getContainer().getIsChangable());
    }

    @Override
    public SanStorageModelBase flush() {
        return Driver.driver.flush();
    }

    public void activateItemsUpdate() {
        sanStorageTargetToLunList.activateItemsUpdate();
    }

    public void disableItemsUpdate() {
        sanStorageTargetToLunList.disableItemsUpdate();
    }

    public boolean isDiscoverPanelFocused() {
        return iscsiDiscoverTargetsView.isDiscoverPanelFocused();
    }

}
