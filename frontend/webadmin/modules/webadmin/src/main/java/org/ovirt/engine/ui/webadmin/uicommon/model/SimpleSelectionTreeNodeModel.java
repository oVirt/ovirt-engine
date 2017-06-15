package org.ovirt.engine.ui.webadmin.uicommon.model;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.TreeNodeModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * A {@link TreeNodeModel} for {@link SelectionTreeNodeModel} instances
 */
public class SimpleSelectionTreeNodeModel implements TreeNodeModel<SelectionTreeNodeModel, SimpleSelectionTreeNodeModel> {

    /**
     * Build from a list of {@link SelectionTreeNodeModel} instances
     */
    public static List<SimpleSelectionTreeNodeModel> fromList(List<SelectionTreeNodeModel> list) {
        List<SimpleSelectionTreeNodeModel> result = new ArrayList<>();
        for (SelectionTreeNodeModel selectionTreeNodeModel : list) {
            result.add(new SimpleSelectionTreeNodeModel(selectionTreeNodeModel));
        }
        return result;
    }

    /**
     * Build from a single {@link SelectionTreeNodeModel} instance
     */
    public static SimpleSelectionTreeNodeModel fromModel(SelectionTreeNodeModel model) {
        return new SimpleSelectionTreeNodeModel(model);
    }

    private final List<SimpleSelectionTreeNodeModel> children;
    private SimpleSelectionTreeNodeModel parent;

    private final EventBus eventBus;
    private final SelectionTreeNodeModel model;

    protected SimpleSelectionTreeNodeModel(SelectionTreeNodeModel model) {
        this.eventBus = ClientGinjectorProvider.getEventBus();
        this.model = model;

        // Build children list using depth-first recursion
        this.children = new ArrayList<>();
        for (SelectionTreeNodeModel childModel : model.getChildren()) {
            SimpleSelectionTreeNodeModel child = new SimpleSelectionTreeNodeModel(childModel);
            child.parent = this;
            children.add(child);
        }

        // Add selection listener
        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsSelectedNullable".equals(args.propertyName)) { //$NON-NLS-1$
                SelectionEvent.fire(SimpleSelectionTreeNodeModel.this, SimpleSelectionTreeNodeModel.this);
            }
        });
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<SimpleSelectionTreeNodeModel> handler) {
        return eventBus.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }

    @Override
    public List<SimpleSelectionTreeNodeModel> getChildren() {
        return children;
    }

    @Override
    public SimpleSelectionTreeNodeModel getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return model.getDescription();
    }

    @Override
    public boolean getSelected() {
        return model.getIsSelectedNullable() == null ? false : model.getIsSelectedNullable();
    }

    public Boolean getIsSelectedNullable() {
        return model.getIsSelectedNullable();
    }

    @Override
    public boolean isEditable() {
        return model.getIsChangable();
    }

    @Override
    public void setSelected(boolean value) {
        model.setIsSelectedNullable(value);
    }

}
