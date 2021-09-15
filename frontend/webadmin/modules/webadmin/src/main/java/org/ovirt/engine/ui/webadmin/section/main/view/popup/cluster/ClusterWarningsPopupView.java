package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.ovirt.engine.core.common.businessentities.ClusterEditWarnings;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.AlertWithIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterWarningsModel;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterWarningsPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

public class ClusterWarningsPopupView
        extends AbstractModelBoundPopupView<ClusterWarningsModel>
        implements ClusterWarningsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ClusterWarningsModel, ClusterWarningsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ClusterWarningsPopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<ClusterWarningsPopupView> {
    }

    interface WarningTemplates extends SafeHtmlTemplates {

        @Template("<div>{0}</div>")
        SafeHtml warningTitle(String text);

        @Template("<ul>{0}</ul>")
        SafeHtml warningList(SafeHtml safeHtml);

        @Template("<li>{0}</li>")
        SafeHtml warning(String entityName);

        @Template("<li>{0} [{1}]</li>")
        SafeHtml warningWithDetails(String entityName, String details);
    }

    private static final int DETAILS_COUNT_LIMIT = 30;

    private final Driver driver;
    private final WarningTemplates warningTemplates;

    @UiField
    @Ignore
    AlertWithIcon hostWarningsAlert;

    @UiField
    @Ignore
    HTML hostWarnings;

    @UiField
    @Ignore
    AlertWithIcon vmWarningsAlert;

    @UiField
    @Ignore
    HTML vmWarnings;

    @Inject
    public ClusterWarningsPopupView(EventBus eventBus, Driver driver,
                                    ViewUiBinder uiBinder, ViewIdHandler viewIdHandler,
                                    WarningTemplates warningTemplates) {
        super(eventBus);
        this.driver = driver;
        this.warningTemplates = warningTemplates;

        initWidget(uiBinder.createAndBindUi(this));
        viewIdHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(ClusterWarningsModel model) {
        driver.edit(model);

        hostWarnings.setHTML(buildWarnings(model.getHostWarnings()));
        vmWarnings.setHTML(buildWarnings(model.getVmWarnings()));
    }

    private SafeHtml buildWarnings(List<ClusterEditWarnings.Warning> warnings) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();

        for (ClusterEditWarnings.Warning warning : warnings) {
            builder.append(warningTemplates.warningTitle(localize(warning.getMainMessage())));
            builder.append(warningTemplates.warningList(buildWarningDetails(warning.getDetailsByName())));
        }

        return builder.toSafeHtml();
    }

    private static String localize(String warning) {
        return Frontend.getInstance().getAppErrorsTranslator().translateErrorTextSingle(warning);
    }

    private SafeHtml buildWarningDetails(Map<String, String> detailsByName) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        int detailsCount = 0;
        for (Map.Entry<String, String> entry : detailsByName.entrySet()) {
            if (detailsCount == DETAILS_COUNT_LIMIT) {
                builder.append(warningTemplates.warning(AssetProvider.getConstants().andMore()));
                break;
            }
            detailsCount++;

            String name = entry.getKey();
            if (entry.getValue() != null) {
                builder.append(warningTemplates.warningWithDetails(name, entry.getValue()));
            } else {
                builder.append(warningTemplates.warning(name));
            }
        }
        return builder.toSafeHtml();
    }

    @Override
    public void init(ClusterWarningsModel model) {
        hostWarningsAlert.setAlertType(AlertType.WARNING);
        hostWarningsAlert.setText(AssetProvider.getConstants().clusterEditHostTitle());
        hostWarningsAlert.setVisible(!model.getHostWarnings().isEmpty());
        hostWarnings.setVisible(!model.getHostWarnings().isEmpty());

        vmWarningsAlert.setAlertType(AlertType.WARNING);
        vmWarningsAlert.setText(AssetProvider.getConstants().clusterEditVmtTitle());
        vmWarningsAlert.setVisible(!model.getVmWarnings().isEmpty());
        vmWarnings.setVisible(!model.getVmWarnings().isEmpty());
    }

    @Override
    public ClusterWarningsModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
