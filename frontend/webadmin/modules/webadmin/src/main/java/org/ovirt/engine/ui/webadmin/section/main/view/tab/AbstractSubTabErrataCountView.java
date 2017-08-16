package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.ErrataCounts;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;
import org.ovirt.engine.core.common.businessentities.HasErrata;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractSubTabPresenter;
import org.ovirt.engine.ui.common.uicommon.model.DetailTabModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.AbstractUiCommandButton;
import org.ovirt.engine.ui.common.widget.UiCommandLink;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel.Type;
import org.ovirt.engine.ui.uicommonweb.models.AbstractErrataCountModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public abstract class AbstractSubTabErrataCountView<I extends HasErrata, M extends ListWithDetailsModel, C extends AbstractErrataCountModel>
    extends AbstractSubTabFormView<I, M, C> implements Editor<AbstractErrataCountModel>,
        AbstractSubTabPresenter.ViewDef<I> {

    interface ViewUiBinder extends UiBinder<Widget, AbstractSubTabErrataCountView<?, ?, ?>> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AbstractSubTabErrataCountView<?, ?, ?>> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField(provided = true)
    GeneralFormPanel formPanel;

    @Ignore
    UiCommandLink totalSecurity;

    @Ignore
    UiCommandLink totalBugFix;

    @Ignore
    UiCommandLink totalEnhancement;

    FormBuilder formBuilder;

    @UiField
    Image progressDotsImage;

    @UiField
    @Ignore
    AlertPanel errorMessagePanel;

    @Inject
    public AbstractSubTabErrataCountView(DetailTabModelProvider<M, C> modelProvider) {
        super(modelProvider);

        // Init form panel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        errorMessagePanel.setType(Type.WARNING);

        showProgress();

        // at this point, only the loading image is visible.

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 3);

        formBuilder.setRelativeColumnWidth(0, 3);

        totalSecurity = new UiCommandLink();
        totalBugFix = new UiCommandLink();
        totalEnhancement = new UiCommandLink();

        formBuilder.addFormItem(new FormItem(constants.totalSecurity(), totalSecurity, 0, 0));
        formBuilder.addFormItem(new FormItem(constants.totalBugFix(), totalBugFix, 1, 0));
        formBuilder.addFormItem(new FormItem(constants.totalEnhancement(), totalEnhancement, 2, 0));
    }

    public void showProgress() {
        progressDotsImage.setVisible(true);
        errorMessagePanel.setVisible(false);
        formPanel.setVisible(false);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(I selectedItem) {
        formBuilder.update(getDetailModel());
    }

    public void clearErrorMessage() {
        errorMessagePanel.clearMessages();
        errorMessagePanel.setVisible(false);
    }

    public void showErrorMessage(SafeHtml message) {
        formPanel.setVisible(false);
        progressDotsImage.setVisible(false);

        errorMessagePanel.clearMessages();
        errorMessagePanel.setVisible(true);
        errorMessagePanel.addMessage(message);
    }

    public void showCounts(ErrataCounts errataCounts) {
        clearErrorMessage();
        progressDotsImage.setVisible(false);

        getTotalSecurity().setLabel(buildSecurityString(errataCounts));
        getTotalBugFix().setLabel(String.valueOf(errataCounts.getCountByType(ErrataType.BUGFIX)));
        getTotalEnhancement().setLabel(String.valueOf(
                errataCounts.getCountByType(ErrataType.ENHANCEMENT)));

        formPanel.setVisible(true);
    }

    public AbstractUiCommandButton getTotalSecurity() {
        return totalSecurity;
    }

    public AbstractUiCommandButton getTotalBugFix() {
        return totalBugFix;
    }

    public AbstractUiCommandButton getTotalEnhancement() {
        return totalEnhancement;
    }

    private String buildSecurityString(ErrataCounts counts) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(counts.getCountByType(ErrataType.SECURITY)));
        builder.append(" ("); //$NON-NLS-1$
        builder.append(String.valueOf(counts.getCountByTypeAndSeverity(ErrataType.SECURITY, ErrataSeverity.CRITICAL)));
        builder.append(" "); //$NON-NLS-1$
        builder.append(constants.critical());
        builder.append(", "); //$NON-NLS-1$
        builder.append(String.valueOf(counts.getCountByTypeAndSeverity(ErrataType.SECURITY, ErrataSeverity.IMPORTANT)));
        builder.append(" "); //$NON-NLS-1$
        builder.append(constants.important());
        builder.append(")"); //$NON-NLS-1$
        return builder.toString();
    }
}
