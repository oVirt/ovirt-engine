package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.HasColumns;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractIconTypeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.ErrataFilterValue;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Renders a grid of errata (singular: Erratum) and a panel of check boxes
 * (ErrataFilterPanel) which allow the user to filter the grid (client-side).
 */
public class ErrataTableView extends ResizeComposite {

    interface ViewUiBinder extends UiBinder<LayoutPanel, ErrataTableView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String errataSummaryLabel();
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField(provided=true)
    EntityModelCellTable<AbstractErrataListModel> errataTable;

    @UiField
    ScrollPanel scrollPanel;

    @UiField
    ErrataFilterPanel errataFilterPanel;

    private List<HandlerRegistration> selectionHandlers = new ArrayList<>();

    protected AbstractErrataListModel errataListModel;

    public ErrataTableView() {
        createErrataTable();
        initErrataGrid(errataTable);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initFilterPanel();
    }

    private void createErrataTable() {
        errataTable = new EntityModelCellTable<AbstractErrataListModel>(false, true) {
            //Override the addSelectionChangeHandler method to prevent a class cast exception. The EntityModelCellTable
            //expect to be handed a list of EntityModels, but the ErrataListModel is returning a list of Erratums. If
            //I rework the list model to return EntityModel<Erratum> I get another class cast exception on something
            //that expects it to return a straight Erratum.
            public void addSelectionChangeHandler() {
                // Handle selection
                getSelectionModel().addSelectionChangeHandler(new Handler() {
                    @Override
                    public void onSelectionChange(SelectionChangeEvent event) {
                        if (errataListModel == null || errataListModel.getItems() == null) {
                            return;
                        }

                        SelectionModel<?> selectionModel = errataTable.getSelectionModel();
                        Erratum selectedObject = (Erratum) ((SingleSelectionModel<?>) selectionModel).getSelectedObject();
                        clearCurrentSelectedItems();
                        if (selectedObject != null) {
                            errataListModel.setSelectedItem(selectedObject);
                        }
                    }

                    private void clearCurrentSelectedItems() {
                        errataListModel.setSelectedItems(null);
                        errataListModel.setSelectedItem(null);
                    }
                });
            }
        };
    }

    public void init(AbstractErrataListModel errataListModel) {
        this.errataListModel = errataListModel;
        errataTable.setLoadingState(LoadingState.LOADING);
        updateFilterPanel();
    }

    private void initFilterPanel() {

        // Handle the filter panel's checkboxes values changing -> simple view update (re-run client-side filter)
        //
        ValueChangeHandler<ErrataFilterValue> handler = event -> {
            errataListModel.setItemsFilter(event.getValue());
            errataListModel.reFilter();
        };
        errataFilterPanel.addValueChangeHandler(handler);
    }

    public void addSelectionChangeHandler(SelectionChangeEvent.Handler selectionHandler) {
        selectionHandlers.add(errataTable.getSelectionModel().addSelectionChangeHandler(selectionHandler));
    }

    public Erratum getSelectedErratum() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        SingleSelectionModel<Erratum> selectionModel = (SingleSelectionModel) errataTable.getSelectionModel();
        return selectionModel.getSelectedObject();
    }

    public static void initErrataGrid(HasColumns grid) {
        initErrataGrid(grid, false, null);
    }

    /**
     * Setup the columns in the errata grid. This configuration is also used in MainEngineErrataView.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void initErrataGrid(HasColumns grid, boolean isEngineErrataView,
            final PlaceTransitionHandler transitionHandler) {
        grid.enableColumnResizing();
        AbstractIconTypeColumn<Erratum> errataTypeColumn = new AbstractIconTypeColumn<Erratum>() {
            @Override
            public IconType getValue(Erratum erratum) {
                if (erratum.getType() == ErrataType.BUGFIX) {
                    getCell().setColor(SafeHtmlUtils.fromTrustedString(ErrataType.BUGFIX.getColor()));
                    return IconType.BUG;
                } else if (erratum.getType() == ErrataType.ENHANCEMENT) {
                    getCell().setColor(SafeHtmlUtils.fromTrustedString(ErrataType.ENHANCEMENT.getColor()));
                    return IconType.PLUS_SQUARE;
                } else if (erratum.getType() == ErrataType.SECURITY) {
                    getCell().setColor(SafeHtmlUtils.fromTrustedString(ErrataType.SECURITY.getColor()));
                    return IconType.WARNING;
                }
                return null;
            }
        };
        errataTypeColumn.makeSortable(new ErrataTypeComparator());
        grid.addColumn(errataTypeColumn, "", "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        AbstractTextColumn errataTypeTextColumn = new AbstractTextColumn<Erratum>() {
            @Override
            public String getValue(Erratum erratum) {
                if (erratum.getType() == ErrataType.BUGFIX) {
                    return constants.bug();
                } else if (erratum.getType() == ErrataType.ENHANCEMENT) {
                    return constants.enhancement();
                } else if (erratum.getType() == ErrataType.SECURITY) {
                    return constants.security();
                }
                return constants.unknown();
            }
        };
        errataTypeTextColumn.makeSortable();
        grid.addColumn(errataTypeTextColumn, constants.errataType(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<Erratum> errataSeverityColumn = new AbstractTextColumn<Erratum>() {
            @Override
            public String getValue(Erratum erratum) {
                if (erratum.getSeverity() == ErrataSeverity.CRITICAL) {
                    return constants.critical();
                } else if (erratum.getSeverity() == ErrataSeverity.IMPORTANT) {
                    return constants.important();
                } else if (erratum.getSeverity() == ErrataSeverity.MODERATE) {
                    return constants.moderate();
                }
                return constants.unknown();
            }
        };
        errataSeverityColumn.makeSortable();
        grid.addColumn(errataSeverityColumn, constants.errataSeverity(), "150px"); //$NON-NLS-1$

        AbstractFullDateTimeColumn<Erratum> dateIssuedColumn = new AbstractFullDateTimeColumn<Erratum>(false) {
            @Override
            protected Date getRawValue(Erratum erratum) {
                return erratum.getIssued();
            }
        };
        dateIssuedColumn.makeSortable();
        grid.addColumn(dateIssuedColumn, constants.errataDateIssued(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<Erratum> errataIdColumn;

        if (!isEngineErrataView) {
            errataIdColumn = new AbstractTextColumn<Erratum>() {

                @Override
                public String getValue(Erratum erratum) {
                    return erratum.getId();
                }
            };
        } else {
            errataIdColumn = new AbstractLinkColumn<Erratum>(new FieldUpdater<Erratum, String>() {

                @Override
                public void update(int index, Erratum erratum, String value) {
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put(FragmentParams.NAME.getName(), erratum.getName());
                    //The link was clicked, now fire an event to switch to details.
                    transitionHandler.handlePlaceTransition(
                            WebAdminApplicationPlaces.errataDetailsSubTabPlace, parameters);
                }

            }) {
                @Override
                public String getValue(Erratum erratum) {
                    return erratum.getId();
                }
            };
        }
        errataIdColumn.makeSortable();
        grid.addColumn(errataIdColumn, constants.errataId(), "115px"); //$NON-NLS-1$
        AbstractTextColumn<Erratum> titleColumn = new AbstractTextColumn<Erratum>() {

            @Override
            public String getValue(Erratum erratum) {
                return erratum.getTitle();
            }
        };
        titleColumn.makeSortable();
        grid.addColumn(titleColumn, constants.errataTitle(), "290px"); //$NON-NLS-1$

    }

    @Override
    public void onResize() {
        super.onResize();
        updateScrollPanelHeight();
    }

    public void clearHandlers() {
        //Clean up the handlers.
        for (HandlerRegistration handler: this.selectionHandlers) {
            handler.removeHandler();
        }
    }

    private void updateScrollPanelHeight() {
        //Set the scroll
        Double newHeight = (double) (getOffsetHeight() - errataFilterPanel.getOffsetHeight());
        if (newHeight.intValue() > 0) {
            scrollPanel.getElement().getStyle().setHeight(newHeight, Unit.PX);
        }
    }

    public void edit() {
        errataTable.asEditor().edit(errataListModel);
        updateFilterPanel();
        errataTable.setLoadingState(LoadingState.LOADED);
    }

    private void updateFilterPanel() {
        ErrataFilterValue filterValue = errataListModel.getItemsFilter();
        if (filterValue != null) {
            this.errataFilterPanel.init(filterValue.isSecurity(), filterValue.isBugs(), filterValue.isEnhancements());
        }
    }

    public EntityModelCellTable<AbstractErrataListModel> getErrataTable() {
        return errataTable;
    }

    private static class ErrataTypeComparator implements Comparator<Erratum>, Serializable {

        @Override
        public int compare(Erratum erratum0, Erratum erratum1) {
            return erratum0.getType().compareTo(erratum1.getType());
        }
    }
}
