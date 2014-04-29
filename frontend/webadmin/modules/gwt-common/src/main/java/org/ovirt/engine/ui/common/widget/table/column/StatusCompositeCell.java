package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.ovirt.engine.core.common.businessentities.VM;

import java.util.List;

public class StatusCompositeCell extends CompositeCell<VM> {
    public interface StatusCompositeCellResources extends ClientBundle {
        @ClientBundle.Source("org/ovirt/engine/ui/common/css/StatusCompositeCell.css")
        StatusCompositeCellCss statusCompositeCellCss();
    }

    public interface StatusCompositeCellCss extends CssResource {
        String divInlineBlock();
    }

    private static final StatusCompositeCellResources RESOURCES = GWT.create(StatusCompositeCellResources.class);
    private final StatusCompositeCellCss style;
    private final List<HasCell<VM, ?>> hasCells;

    public StatusCompositeCell(List<HasCell<VM, ?>> hasCells) {
        super(hasCells);
        this.hasCells = hasCells;
        style = RESOURCES.statusCompositeCellCss();
        style.ensureInjected();
    }

    @Override
    public void render(Cell.Context context, VM value, SafeHtmlBuilder sb) {
        for (HasCell<VM, ?> hasCell : hasCells) {
            render(context, value, sb, hasCell);
        }
    }

    protected <T> void render(Cell.Context context, VM value,
                              SafeHtmlBuilder sb, HasCell<VM, T> hasCell) {
        Cell<T> cell = hasCell.getCell();
        if (cell instanceof HasStyleClass) {
            ((HasStyleClass) cell).setStyleClass(style.divInlineBlock());
        }
        cell.render(context, hasCell.getValue(value), sb);
    }
}
