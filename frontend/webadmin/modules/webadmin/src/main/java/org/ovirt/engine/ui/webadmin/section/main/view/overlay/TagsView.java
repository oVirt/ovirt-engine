package org.ovirt.engine.ui.webadmin.section.main.view.overlay;

import java.util.Collection;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.overlay.TagsPresenterWidget;
import org.ovirt.engine.ui.webadmin.uicommon.model.TagModelProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.inject.Inject;

public class TagsView extends AbstractView implements TagsPresenterWidget.ViewDef {

    private static final int INDENT_WIDTH = 15;

    interface ViewUiBinder extends UiBinder<Container, TagsView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    Button closeButton;

    @UiField
    Button addTagButton;

    @UiField
    Column emptyTagsColumn;

    @UiField
    ListGroup rootListGroup;

    private final TagModelProvider tagModelProvider;

    @Inject
    public TagsView(TagModelProvider modelProvider) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        this.tagModelProvider = modelProvider;
    }

    @Override
    public void updateTags(Collection<TagModel> tags) {
        emptyTagsColumn.setVisible(tags.isEmpty());
        rootListGroup.clear();
        for (TagModel model: tags) {
            addChildTags(model.getChildren(), rootListGroup, 0);
        }
    }

    private void addChildTags(List<TagModel> tagModels, HasWidgets group, final int indent) {
        int newIndent = indent + 1;
        for (TagModel model: tagModels) {
            ListGroupItem tagItem = new ListGroupItem();
            tagItem.getElement().getStyle().setPaddingLeft(indent * INDENT_WIDTH, Unit.PX);
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(createActivateButton(model));
            buttonGroup.add(createAddButton(model));
            buttonGroup.add(createEditButton(model));
            buttonGroup.add(createRemoveButton(model));
            buttonGroup.addStyleName(Styles.PULL_RIGHT);
            tagItem.add(buttonGroup);
            tagItem.setText(model.getName().getEntity());
            group.add(tagItem);
            List<TagModel> children = model.getChildren();
            if (children != null && !children.isEmpty()) {
                addChildTags(model.getChildren(), group, newIndent);
            }
        }
    }

    private Button createActivateButton(final TagModel model) {
        final Button result = new Button(model.getSelection() ? constants.deactivateTag() : constants.activateTag());
        result.addClickHandler(e -> {
            model.setSelection(model.getSelection() != null ? !model.getSelection() : true);
            tagModelProvider.getSelectionModel().setSelected(model, true);
            updateTags(tagModelProvider.getModel().getItems());
        });
        return result;
    }

    private Button createEditButton(final TagModel model) {
        Button result = new Button(constants.editTag());
        result.addClickHandler(e -> {
            tagModelProvider.getSelectionModel().setSelected(model, true);
            Scheduler.get().scheduleDeferred(() -> {
                tagModelProvider.getModel().executeCommand(tagModelProvider.getModel().getEditCommand());
            });
        });
        return result;
    }

    private Button createRemoveButton(final TagModel model) {
        Button result = new Button(constants.removeTag());
        result.addClickHandler(e -> {
            tagModelProvider.getSelectionModel().setSelected(model, true);
            Scheduler.get().scheduleDeferred(() -> {
                tagModelProvider.getModel().executeCommand(tagModelProvider.getModel().getRemoveCommand());
            });
        });
        return result;
    }

    private Button createAddButton(final TagModel model) {
        Button result = new Button(constants.newTag());
        result.addClickHandler(e -> {
            tagModelProvider.getSelectionModel().setSelected(model, true);
            tagModelProvider.getModel().executeCommand(tagModelProvider.getModel().getNewCommand());
        });
        return result;
    }

    @Override
    public HasClickHandlers getCloseButton() {
        return closeButton;
    }

    @Override
    public HasClickHandlers getAddTagButton() {
        return addTagButton;
    }
}
