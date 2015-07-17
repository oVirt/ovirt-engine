package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ClusterEditWarnings;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterWarningsModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterWarningsPopupPresenterWidget;

import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

public class ClusterWarningsPopupView
        extends AbstractModelBoundPopupView<ClusterWarningsModel>
        implements ClusterWarningsPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ClusterWarningsModel, ClusterWarningsPopupView> {
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

    private final Driver driver;
    private final WarningTemplates warningTemplates;

    @UiField
    @Ignore
    FlowPanel hostPanel;

    @UiField
    @Ignore
    FlowPanel vmPanel;

    @UiField
    @Ignore
    HTML hostWarnings;

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
        for (Map.Entry<String, String> entry : detailsByName.entrySet()) {
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
        hostPanel.setVisible(!model.getHostWarnings().isEmpty());
        vmPanel.setVisible(!model.getVmWarnings().isEmpty());
    }

    @Override
    public ClusterWarningsModel flush() {
        return driver.flush();
    }
}
