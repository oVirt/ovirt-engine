package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.List;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.ListItem;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.UnorderedList;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainWithDetailsPresenter;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Base class for table-based main tab views that work with {@link ListWithDetailsModel}.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            Main model type.
 */
public abstract class AbstractMainWithDetailsTableView<T, M extends ListWithDetailsModel> extends AbstractMainTableView<T, M>
        implements AbstractMainWithDetailsPresenter.ViewDef<T> {

    private static final String OBRAND_MAIN_TAB = "obrand_main_tab"; // $NON-NLS-1$

    protected PlaceTransitionHandler placeTransitionHandler;

    private Column breadCrumbsColumn;

    private final FlowPanel actionSearchPanel = new FlowPanel();

    private Row resultRow;
    private UnorderedList resultList;

    private IsWidget searchPanel;
    private ActionPanelPresenterWidget<?, ?, M> actionPanel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public AbstractMainWithDetailsTableView(MainModelProvider<T, M> modelProvider) {
        super(modelProvider);
        SimpleActionTable<Void, T> table = getTable();
        FlowPanel tableContainer = table.getOuterWidget();
        addBreadCrumbs(tableContainer);
        addActionSearchPanel(tableContainer);
        table.addStyleName(OBRAND_MAIN_TAB);
        table.addStyleName(Styles.CONTAINER_FLUID);
    }

    @Override
    public HandlerRegistration addWindowResizeHandler(ResizeHandler handler) {
        return Window.addResizeHandler(handler);
    }

    @Override
    public void resizeToFullHeight() {
        int tableTop = table.getTableAbsoluteTop();
        if (tableTop > 0) {
            table.setMaxGridHeight(Window.getClientHeight() - tableTop);
            table.updateGridSize();
        }
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AbstractMainWithDetailsPresenter.TYPE_SetSearchPanel) {
            if (content != null) {
                if (actionPanel == null) {
                    searchPanel = content;
                } else {
                    actionPanel.addSearchPanel(content);
                }
            } else {
                searchPanel = null;
                if (actionPanel != null) {
                    actionPanel.addSearchPanel(null);
                }
            }
        } else if (slot == AbstractMainWithDetailsPresenter.TYPE_SetActionPanel) {
            if (content != null) {
                actionSearchPanel.add(content);
                this.actionPanel = (ActionPanelPresenterWidget<?, ?, M>) content;
                if (searchPanel != null) {
                    actionPanel.addSearchPanel(searchPanel);
                }
                addResultPanel(actionPanel);
            } else {
                actionSearchPanel.clear();
                this.actionPanel = null;
                resultRow.clear();
                resultRow = null;
            }
        } else if (slot == AbstractMainWithDetailsPresenter.TYPE_SetBreadCrumbs) {
            breadCrumbsColumn.clear();
            if (content != null) {
                breadCrumbsColumn.add(content);
            }
        } else {
            super.setInSlot(slot, content);
        }
    }

    @Override
    public IsWidget getTableContainer() {
        return super.getTable().getOuterWidget();
    }

    private void addBreadCrumbs(FlowPanel container) {
        Row breadCrumbsRow = new Row();
        breadCrumbsColumn = new Column(ColumnSize.SM_12);
        breadCrumbsRow.add(breadCrumbsColumn);
        container.insert(breadCrumbsRow, 0);
    }

    private void addActionSearchPanel(FlowPanel container) {
        container.insert(actionSearchPanel, 1);
    }

    private void addResultPanel(ActionPanelPresenterWidget<?, ?, M> actionPanel) {
        resultRow = new Row();
        resultRow.addStyleName(PatternflyConstants.PF_TOOLBAR_RESULTS);

        FlowPanel resultColumn = new FlowPanel();
        resultRow.add(resultColumn);

        resultColumn.add(new Paragraph(constants.activeTags() + ":")); // $NON-NLS-1$
        resultList = new UnorderedList();
        resultList.addStyleName(Styles.LIST_INLINE);
        resultList.getElement().getStyle().setPaddingLeft(10, Unit.PX);
        resultColumn.add(resultList);
        resultRow.setVisible(false);
        actionPanel.setFilterResultPanel(resultRow);
    }

    public void setActiveTags(List<TagModel> tags) {
        resultList.clear();
        for (final TagModel tag: tags) {
            ListItem tagItem = new ListItem();
            Span label = new Span();
            label.addStyleName(Styles.LABEL);
            label.addStyleName(PatternflyConstants.PF_LABEL_INFO);
            label.setText(tag.getName().getEntity());
            Anchor deactivateAnchor = new Anchor();
            Span closeIconSpan = new Span();
            closeIconSpan.addStyleName(PatternflyIconType.PF_BASE.getCssName());
            closeIconSpan.addStyleName(PatternflyIconType.PF_CLOSE.getCssName());
            deactivateAnchor.add(closeIconSpan);
            deactivateAnchor.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    tag.setSelection(false);
                }

            });
            label.add(deactivateAnchor);
            tagItem.add(label);
            resultList.add(tagItem);
        }
        resultRow.setVisible(!tags.isEmpty());
    }

    @Override
    public void setPlaceTransitionHandler(PlaceTransitionHandler handler) {
        placeTransitionHandler = handler;
    }

    protected PlaceTransitionHandler getPlaceTransitionHandler() {
        return placeTransitionHandler;
    }

    protected FlowPanel getActionSearchPanel() {
        return actionSearchPanel;
    }
}
