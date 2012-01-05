package org.ovirt.engine.ui.webadmin.widget.tags;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.AbstractActionStackPanelItem;
import org.ovirt.engine.ui.webadmin.widget.action.SimpleActionPanel;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Widget;

public class TagList extends AbstractActionStackPanelItem<TagModelProvider, TagListModel, CellTree> {

    interface WidgetUiBinder extends UiBinder<Widget, TagList> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<TagList> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @WithElementId
    SimpleActionPanel<TagListModel> simpleActionPanel;

    public TagList(TagModelProvider modelProvider) {
        super(modelProvider);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addActionButtons(modelProvider);
    }

    @Override
    protected CellTree createDataDisplayWidget(TagModelProvider modelProvider) {
        TagTreeResources res = GWT.create(TagTreeResources.class);

        CellTree display = new CellTree(modelProvider, null, res);
        display.setAnimationEnabled(true);
        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        modelProvider.setDisplay(display);

        return display;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected SimpleActionPanel<TagListModel> createActionPanel(final TagModelProvider modelProvider) {
        simpleActionPanel = new SimpleActionPanel(modelProvider, modelProvider.getSelectionModel());
        return simpleActionPanel;
    }

    private void addActionButtons(final TagModelProvider modelProvider) {
        simpleActionPanel.addActionButton(new UiCommandButtonDefinition<TagListModel>("New") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getNewCommand();
            }
        });

        simpleActionPanel.addActionButton(new UiCommandButtonDefinition<TagListModel>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getEditCommand();
            }
        });

        simpleActionPanel.addActionButton(new UiCommandButtonDefinition<TagListModel>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });
    }

    interface TagTreeResources extends CellTree.Resources {
        interface TableStyle extends CellTree.Style {
        }

        @Override
        @Source({ CellTree.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TagTree.css" })
        TableStyle cellTreeStyle();
    }
}
