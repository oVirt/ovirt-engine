package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.HasColumns;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractFullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractImageResourceColumn;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataListModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ErrataFilterValue;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.errata.ErrataFilterPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Renders a grid of errata (singular: Erratum) and a panel of checkboxes
 * (ErrataFilterPanel) which allow the user to filter the grid (client-side).
 */
public class ErrataTableView extends Composite {

    interface ViewUiBinder extends UiBinder<HTMLPanel, ErrataTableView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    public interface Style extends CssResource {
        String errataSummaryLabel();
    }

    private final static ApplicationConstants constants = AssetProvider.getConstants();
    private final static ApplicationResources resources = AssetProvider.getResources();

    @UiField(provided=true)
    EntityModelCellTable<AbstractErrataListModel> errataTable;

    @UiField
    ErrataFilterPanel errataFilterPanel;

    protected AbstractErrataListModel errataListModel;

    public ErrataTableView() {
        errataTable = new EntityModelCellTable<AbstractErrataListModel>(false, true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initFilterPanel();
        initErrataGrid(errataTable);
    }

    public void init(AbstractErrataListModel errataListModel) {
        this.errataListModel = errataListModel;
    }

    private void initFilterPanel() {

        // Handle the filter panel's checkboxes values changing -> simple view update (re-run client-side filter)
        //
        ValueChangeHandler<ErrataFilterValue> handler = new ValueChangeHandler<ErrataFilterValue>() {
            @Override
            public void onValueChange(ValueChangeEvent<ErrataFilterValue> event) {
                errataListModel.setItemsFilter(event.getValue());
                errataListModel.reFilter();
            }
        };

        errataFilterPanel.addValueChangeHandler(handler);
    }

    public void addSelectionChangeHandler(SelectionChangeEvent.Handler selectionHandler) {
        errataTable.getSelectionModel().addSelectionChangeHandler(selectionHandler);
    }

    public Erratum getSelectedErratum() {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        SingleSelectionModel<EntityModel<Erratum>> selectionModel = (SingleSelectionModel) errataTable.getSelectionModel();
        Erratum erratum = selectionModel.getSelectedObject().getEntity();

        return erratum;
    }

    /**
     * Setup the columns in the errata grid. This configuration is also used in MainTabEngineErrataView.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void initErrataGrid(HasColumns grid) {

        grid.addColumn(new AbstractImageResourceColumn<EntityModel<Erratum>>() {
            @Override
            public ImageResource getValue(EntityModel<Erratum> erratum) {
                if (erratum.getEntity().getType() == ErrataType.BUGFIX) {
                    return resources.bug();
                }
                else if (erratum.getEntity().getType() == ErrataType.ENHANCEMENT) {
                    return resources.enhancement();
                }
                else if (erratum.getEntity().getType() == ErrataType.SECURITY) {
                    return resources.security();
                }
                return null;
            }
        }, "", "30px"); //$NON-NLS-1$ //$NON-NLS-2$

        grid.addColumn(new AbstractEntityModelTextColumn<Erratum>() {
            @Override
            public String getText(Erratum erratum) {
                if (erratum.getType() == ErrataType.BUGFIX) {
                    return constants.bug();
                }
                else if (erratum.getType() == ErrataType.ENHANCEMENT) {
                    return constants.enhancement();
                }
                else if (erratum.getType() == ErrataType.SECURITY) {
                    return constants.security();
                }
                return constants.unknown();
            }
        }, constants.errataType(), "150px"); //$NON-NLS-1$

        grid.addColumn(new AbstractEntityModelTextColumn<Erratum>() {
            @Override
            public String getText(Erratum erratum) {
                if (erratum.getSeverity() == ErrataSeverity.CRITICAL) {
                    return constants.critical();
                }
                else if (erratum.getSeverity() == ErrataSeverity.IMPORTANT) {
                    return constants.important();
                }
                else if (erratum.getSeverity() == ErrataSeverity.MODERATE) {
                    return constants.moderate();
                }
                return constants.unknown();
            }
        }, constants.errataSeverity(), "150px"); //$NON-NLS-1$

        grid.addColumn(new AbstractFullDateTimeColumn<EntityModel<Erratum>>(false) {
            @Override
            protected Date getRawValue(EntityModel<Erratum> erratum) {
                return erratum.getEntity().getIssued();
            }
        }, constants.errataDateIssued(), "100px"); //$NON-NLS-1$

        grid.addColumn(new AbstractEntityModelTextColumn<Erratum>() {

            @Override
            public String getText(Erratum erratum) {
                return erratum.getId();
            }
        }, constants.errataId(), "75px"); //$NON-NLS-1$
        grid.addColumn(new AbstractEntityModelTextColumn<Erratum>() {

            @Override
            public String getText(Erratum erratum) {
                return erratum.getTitle();
            }
        }, constants.errataTitle(), "200px"); //$NON-NLS-1$

    }

    public void edit() {
        errataTable.asEditor().edit(errataListModel);
    }

    public EntityModelCellTable<AbstractErrataListModel> getErrataTable() {
        return errataTable;
    }
}
