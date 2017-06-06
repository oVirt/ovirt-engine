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
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.OvirtBreadCrumbs;
import org.ovirt.engine.ui.common.widget.PatternflyIconType;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.PatternflyActionPanel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AbstractMainTabWithDetailsPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.DetailsTransitionHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
public abstract class AbstractMainTabWithDetailsTableView<T, M extends ListWithDetailsModel> extends AbstractMainTabTableView<T, M>
        implements AbstractMainTabWithDetailsPresenter.ViewDef<T> {

    private static final String OBRAND_MAIN_TAB = "obrand_main_tab"; // $NON-NLS-1$

    protected DetailsTransitionHandler<T> transitionHandler;

    private Column breadCrumbsColumn;

    private Row resultRow;
    private UnorderedList resultList;
    private PatternflyActionPanel actionPanel;

    private boolean tableInitialized = false;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public AbstractMainTabWithDetailsTableView(MainModelProvider<T, M> modelProvider) {
        super(modelProvider);
    }

    @Override
    public void setDetailPlaceTransitionHandler(DetailsTransitionHandler<T> handler) {
        this.transitionHandler = handler;
    }

    @Override
    public SimpleActionTable<T> getTable() {
        SimpleActionTable<T> result = super.getTable();
        if (!tableInitialized && result.getOuterWidget() instanceof FlowPanel) {
            FlowPanel tableContainer = (FlowPanel) result.getOuterWidget();
            addBreadCrumbs(tableContainer);
            addActionPanel(tableContainer);
            result.addStyleName(OBRAND_MAIN_TAB);
            addResultPanel();
            tableInitialized = true;
        }
        return result;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == AbstractMainTabWithDetailsPresenter.TYPE_SetSearchPanel) {
            if (content != null) {
                actionPanel.setSearchPanel(content);
                actionPanel.setVisible(true);
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

    private void addActionPanel(FlowPanel container) {
        actionPanel = new PatternflyActionPanel();
        container.insert(actionPanel, 1);
    }

    private void addResultPanel() {
        resultRow = new Row();
        resultRow.addStyleName(PatternflyConstants.PF_TOOLBAR_RESULTS);

        Column resultColumn = new Column(ColumnSize.SM_12);
        resultRow.add(resultColumn);

        resultColumn.add(new Paragraph(constants.activeTags() + ":")); // $NON-NLS-1$
        resultList = new UnorderedList();
        resultList.addStyleName(Styles.LIST_INLINE);
        resultColumn.add(resultList);
        resultRow.setVisible(false);
        actionPanel.addResult(resultRow);
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
    public void setBreadCrumbs(OvirtBreadCrumbs<T, ?> breadCrumbs) {
        if (breadCrumbsColumn != null) {
            breadCrumbsColumn.clear();
            breadCrumbsColumn.add(breadCrumbs);
        }
    }

    protected void addButtonToActionGroup(ActionButton button) {
        actionPanel.addButtonToActionGroup(button);
    }

    protected void addMenuItemToKebab(ActionButton menuItem) {
        actionPanel.addMenuItemToKebab(menuItem);
    }

    protected void addDividerToKebab() {
        actionPanel.addDividerToKebab();
    }
}
