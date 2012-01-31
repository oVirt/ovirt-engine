package org.ovirt.engine.ui.userportal.section.main.view.tab.basic;

import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListItemPresenterWidget;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalImageButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MainTabBasicListItemView extends AbstractView implements MainTabBasicListItemPresenterWidget.ViewDef {

    @UiField
    @Ignore
    Image osType;

    @UiField
    @Ignore
    Label machineStatus;

    @UiField
    @Path("name")
    Label vmName;

    @UiField
    @Ignore
    FlowPanel buttonsPanel;

    @UiField
    public Style style;

    interface Driver extends SimpleBeanEditorDriver<UserPortalItemModel, MainTabBasicListItemView> {
        Driver driver = GWT.create(Driver.class);
    }

    public interface Style extends CssResource {
        String buttonStyle();
    }

    interface ViewUiBinder extends UiBinder<Widget, MainTabBasicListItemView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Inject
    public MainTabBasicListItemView(ApplicationResources applicationResources) {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        osType.setUrl(applicationResources.unassignedLargeImage().getURL());
        machineStatus.setText("Machine is Down");
        vmName.setText("some name");

        addButton(new UserPortalImageButtonDefinition<UserPortalItemModel>("",
                applicationResources.playIcon(),
                applicationResources.playDisabledIcon()) {

            @Override
            protected UICommand resolveCommand() {
                return null;
            }

        });

        addButton(new UserPortalImageButtonDefinition<UserPortalItemModel>("",
                applicationResources.stopIcon(),
                applicationResources.stopDisabledIcon()) {

            @Override
            protected UICommand resolveCommand() {
                return null;
            }

        });

        addButton(new UserPortalImageButtonDefinition<UserPortalItemModel>("",
                applicationResources.pauseIcon(),
                applicationResources.pauseDisabledIcon()) {

            @Override
            protected UICommand resolveCommand() {
                return null;
            }

        });

        Driver.driver.initialize(this);

    }

    private void addButton(final UserPortalImageButtonDefinition<UserPortalItemModel> buttonDefinition) {
        PushButton button = new PushButton();
        button.getUpFace().setHTML(buttonDefinition.getEnabledHtml());
        button.getUpDisabledFace().setHTML(buttonDefinition.getDisabledHtml());
        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                buttonDefinition.onClick(null);
            }
        });

        button.addStyleName(style.buttonStyle());

        buttonsPanel.add(button);

    }

    @Override
    public void edit(UserPortalItemModel model) {
        Driver.driver.edit(model);
    }

    @Override
    public UserPortalItemModel flush() {
        return Driver.driver.flush();
    }

}
