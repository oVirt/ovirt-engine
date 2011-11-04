package org.ovirt.engine.ui.webadmin.widget.tags;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagListModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;
import org.ovirt.engine.ui.webadmin.widget.AbstractActionStackPanelItem;
import org.ovirt.engine.ui.webadmin.widget.SimpleActionPanel;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TagList extends AbstractActionStackPanelItem<TagListModel> {

    interface WidgetUiBinder extends UiBinder<Widget, TagList> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    VerticalPanel container;

    public TagList(TagModelProvider modelProvider) {
        super(getTreeDisplayWidget(modelProvider), getActionPanel(modelProvider));
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        container.setCellHeight(actionPanel, "18px");
    }

    static Widget getTreeDisplayWidget(TagModelProvider modelProvider) {
        TagTreeResources res = GWT.create(TagTreeResources.class);

        CellTree display = new CellTree(modelProvider, null, res);
        display.setAnimationEnabled(true);
        display.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        modelProvider.setDisplay(display);

        return display;
    }

    static SimpleActionPanel<TagListModel> getActionPanel(final TagModelProvider modelProvider) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        SimpleActionPanel<TagListModel> simpleActionPanel =
                new SimpleActionPanel(modelProvider, modelProvider.getSelectionModel());

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

        return simpleActionPanel;
    }

    interface TagTreeResources extends CellTree.Resources {
        interface TableStyle extends CellTree.Style {
        }

        @Override
        @Source({ CellTree.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/TagTree.css" })
        TableStyle cellTreeStyle();
    }

}
