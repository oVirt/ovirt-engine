package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.constants.Attributes;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.PatternflyStyles;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MenuPresenterWidget;
import org.ovirt.engine.ui.webadmin.uimode.UiModeData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class MenuView extends AbstractView implements MenuPresenterWidget.ViewDef {

    private static final String SECONDARY_POST_FIX = "-secondary"; //$NON-NLS-1$
    private static final String ID = "id"; //$NON-NLS-1$
    private static final String COMPUTE = "compute"; // $NON-NLS-1$
    private static final String NETWORK = "network"; // $NON-NLS-1$
    private static final String STORAGE = "storage"; // $NON-NLS-1$
    private static final String ADMINISTRATION = "admin"; // $NON-NLS-1$

    interface ViewUiBinder extends UiBinder<Widget, MenuView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MenuView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    ListGroup menuListGroup;

    @UiField
    ListGroupItem computeSecondaryItem;
    @UiField
    FlowPanel computeSecondaryContainer;
    @UiField
    Anchor computeSecondaryHeader;
    @UiField
    Anchor computePrimaryHeader;

    @UiField
    ListGroupItem networkSecondaryItem;
    @UiField
    FlowPanel networkSecondaryContainer;
    @UiField
    Anchor networkSecondaryHeader;
    @UiField
    Anchor networkPrimaryHeader;

    @UiField
    ListGroupItem storageSecondaryItem;
    @UiField
    FlowPanel storageSecondaryContainer;
    @UiField
    Anchor storageSecondaryHeader;
    @UiField
    Anchor storagePrimaryHeader;

    @UiField
    ListGroupItem administrationSecondaryItem;
    @UiField
    FlowPanel administrationSecondaryContainer;
    @UiField
    Anchor administrationSecondaryHeader;
    @UiField
    Anchor administrationPrimaryHeader;

    @UiField
    ListGroupItem eventsPrimaryItem;
    @UiField
    Anchor eventsAnchor;

    /* Anchors */
    @WithElementId
    @UiField
    Anchor vmsAnchor;
    @WithElementId
    @UiField
    Anchor templatesAnchor;
    @WithElementId
    @UiField
    Anchor poolsAnchor;
    @WithElementId
    @UiField
    Anchor hostsAnchor;
    @WithElementId
    @UiField
    Anchor dataCentersAnchor;
    @WithElementId
    @UiField
    Anchor clustersAnchor;

    @WithElementId
    @UiField
    Anchor vnicProfilesAnchor;
    @WithElementId
    @UiField
    Anchor networksAnchor;

    @WithElementId
    @UiField
    Anchor dataCentersStorageAnchor;
    @WithElementId
    @UiField
    Anchor clustersStorageAnchor;
    @WithElementId
    @UiField
    Anchor domainsAnchor;
    @WithElementId
    @UiField
    Anchor volumesAnchor;
    @WithElementId
    @UiField
    Anchor disksAnchor;

    @WithElementId
    @UiField
    Anchor providersAnchor;
    @WithElementId
    @UiField
    Anchor quotasAnchor;
    @WithElementId
    @UiField
    Anchor sessionsAnchor;
    @WithElementId
    @UiField
    Anchor usersAnchor;
    @WithElementId
    @UiField
    Anchor errataAnchor;
    @WithElementId
    @UiField
    Anchor configureAnchor;

    private final Map<String, ListGroupItem> hrefToGroupLabelMap = new HashMap<>();
    private final List<Anchor> anchorList = new ArrayList<>();

    public MenuView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        setTargetAndId();
        updateBasedonMode(UiModeData.getUiMode());
        populateHrefToGroupMap(UiModeData.getUiMode());
        populateAnchorList();
    }

    private void setTargetAndId() {
        computeSecondaryItem.getElement().setAttribute(Attributes.DATA_TARGET,
                hashifyString(COMPUTE + SECONDARY_POST_FIX));
        computeSecondaryContainer.getElement().setAttribute(ID, COMPUTE + SECONDARY_POST_FIX);
        computeSecondaryHeader.getElement().setAttribute(Attributes.DATA_TOGGLE, PatternflyStyles.NAV_COLLAPSE_SECONDARY_NAV);
        computePrimaryHeader.getElement().setAttribute(ID, "id-" + COMPUTE); // $NON-NLS-1$

        networkSecondaryItem.getElement().setAttribute(Attributes.DATA_TARGET,
                hashifyString(NETWORK + SECONDARY_POST_FIX));
        networkSecondaryContainer.getElement().setAttribute(ID, NETWORK + SECONDARY_POST_FIX);
        networkSecondaryHeader.getElement().setAttribute(Attributes.DATA_TOGGLE, PatternflyStyles.NAV_COLLAPSE_SECONDARY_NAV);
        networkPrimaryHeader.getElement().setAttribute(ID, "id-" + NETWORK); // $NON-NLS-1$

        storageSecondaryItem.getElement().setAttribute(Attributes.DATA_TARGET,
                hashifyString(STORAGE + SECONDARY_POST_FIX));
        storageSecondaryContainer.getElement().setAttribute(ID, STORAGE + SECONDARY_POST_FIX);
        storageSecondaryHeader.getElement().setAttribute(Attributes.DATA_TOGGLE, PatternflyStyles.NAV_COLLAPSE_SECONDARY_NAV);
        storagePrimaryHeader.getElement().setAttribute(ID, "id-" + STORAGE); // $NON-NLS-1$

        administrationSecondaryItem.getElement().setAttribute(Attributes.DATA_TARGET,
                hashifyString(ADMINISTRATION + SECONDARY_POST_FIX));
        administrationSecondaryContainer.getElement().setAttribute(ID, ADMINISTRATION + SECONDARY_POST_FIX);
        administrationSecondaryHeader.getElement().setAttribute(Attributes.DATA_TOGGLE, PatternflyStyles.NAV_COLLAPSE_SECONDARY_NAV);
        administrationPrimaryHeader.getElement().setAttribute(ID, "id-" + ADMINISTRATION); // $NON-NLS-1$

    }

    private void updateBasedonMode(ApplicationMode applicationMode) {
        switch (applicationMode) {
        case VirtOnly:
        case AllModes:
            clustersStorageAnchor.setVisible(false);
            break;
        case GlusterOnly:
            computeSecondaryHeader.setVisible(false);
            computePrimaryHeader.setVisible(false);
            vmsAnchor.setVisible(false);
            templatesAnchor.setVisible(false);
            poolsAnchor.setVisible(false);
            hostsAnchor.setVisible(false);
            dataCentersAnchor.setVisible(false);
            clustersAnchor.setVisible(false);
            disksAnchor.setVisible(false);
            providersAnchor.setVisible(false);
            quotasAnchor.setVisible(false);
            break;
        default:
            // Do nothing, we have all we need.
            break;
        }
    }

    @Override
    public HasClickHandlers getConfigureItem() {
        return configureAnchor;
    }

    private String hashifyString(String original) {
        String result = original;
        if (!original.startsWith("#")) { // $NON-NLS-1$
            result = "#" + result; // $NON-NLS-1$
        }
        return result;
    }

    @Override
    public void addMenuItem(int index, String label, String href) {
        ListGroupItem newMenuItem = new ListGroupItem();
        Anchor menuAnchor = new Anchor(hashifyString(href));
        Span iconSpan = new Span();
        // HACK, TODO: implement ability for UI plugins to pass an icon.
        if (index < 0) {
            iconSpan.addStyleName(Styles.FONT_AWESOME_BASE);
            iconSpan.addStyleName(IconType.TACHOMETER.getCssName());
            newMenuItem.addStyleName(Styles.ACTIVE);
            index = 0;
        }
        menuAnchor.add(iconSpan);
        Span labelSpan = new Span();
        labelSpan.setText(label);
        labelSpan.addStyleName(PatternflyStyles.LIST_GROUP_ITEM_VALUE);
        menuAnchor.add(labelSpan);
        newMenuItem.add(menuAnchor);
        // Insert the new menu item into the map.
        hrefToGroupLabelMap.put(href, newMenuItem);
        menuListGroup.insert(newMenuItem, index);
    }

    private void populateHrefToGroupMap(ApplicationMode applicationMode) {
        hrefToGroupLabelMap.put(vmsAnchor.getTargetHistoryToken(), computeSecondaryItem);
        hrefToGroupLabelMap.put(templatesAnchor.getTargetHistoryToken(), computeSecondaryItem);
        hrefToGroupLabelMap.put(poolsAnchor.getTargetHistoryToken(), computeSecondaryItem);
        hrefToGroupLabelMap.put(hostsAnchor.getTargetHistoryToken(), computeSecondaryItem);
        if (!ApplicationMode.GlusterOnly.equals(applicationMode)) {
            hrefToGroupLabelMap.put(dataCentersAnchor.getTargetHistoryToken(), computeSecondaryItem);
            hrefToGroupLabelMap.put(clustersAnchor.getTargetHistoryToken(), computeSecondaryItem);
        }

        hrefToGroupLabelMap.put(vnicProfilesAnchor.getTargetHistoryToken(), networkSecondaryItem);
        hrefToGroupLabelMap.put(networksAnchor.getTargetHistoryToken(), networkSecondaryItem);

        if (ApplicationMode.GlusterOnly.equals(applicationMode)) {
            hrefToGroupLabelMap.put(dataCentersStorageAnchor.getTargetHistoryToken(), storageSecondaryItem);
            hrefToGroupLabelMap.put(clustersStorageAnchor.getTargetHistoryToken(), storageSecondaryItem);
        }
        hrefToGroupLabelMap.put(domainsAnchor.getTargetHistoryToken(), storageSecondaryItem);
        hrefToGroupLabelMap.put(volumesAnchor.getTargetHistoryToken(), storageSecondaryItem);
        hrefToGroupLabelMap.put(disksAnchor.getTargetHistoryToken(), storageSecondaryItem);

        hrefToGroupLabelMap.put(providersAnchor.getTargetHistoryToken(), administrationSecondaryItem);
        hrefToGroupLabelMap.put(quotasAnchor.getTargetHistoryToken(), administrationSecondaryItem);
        hrefToGroupLabelMap.put(sessionsAnchor.getTargetHistoryToken(), administrationSecondaryItem);
        hrefToGroupLabelMap.put(usersAnchor.getTargetHistoryToken(), administrationSecondaryItem);
        hrefToGroupLabelMap.put(errataAnchor.getTargetHistoryToken(), administrationSecondaryItem);

        hrefToGroupLabelMap.put(eventsAnchor.getTargetHistoryToken(), eventsPrimaryItem);
    }

    private void populateAnchorList() {
        anchorList.add(vmsAnchor);
        anchorList.add(templatesAnchor);
        anchorList.add(poolsAnchor);
        anchorList.add(hostsAnchor);
        anchorList.add(dataCentersAnchor);
        anchorList.add(clustersAnchor);
        anchorList.add(vnicProfilesAnchor);
        anchorList.add(networksAnchor);
        anchorList.add(dataCentersStorageAnchor);
        anchorList.add(clustersStorageAnchor);
        anchorList.add(domainsAnchor);
        anchorList.add(volumesAnchor);
        anchorList.add(disksAnchor);
        anchorList.add(providersAnchor);
        anchorList.add(quotasAnchor);
        anchorList.add(sessionsAnchor);
        anchorList.add(usersAnchor);
        anchorList.add(errataAnchor);
        anchorList.add(configureAnchor);

    }

    @Override
    public String getLabelFromHref(String href) {
        ListGroupItem group = hrefToGroupLabelMap.get(href);
        String result = "";
        if (group != null) {
            result = ((Anchor) group.getWidget(1)).getElement().getInnerText().trim();
        }
        return result;
    }

    @Override
    public void setMenuActive(String href) {
        ListGroupItem group = hrefToGroupLabelMap.get(href);
        if (group != null) {
            for (int i = 0; i < menuListGroup.getWidgetCount(); i++) {
                menuListGroup.getWidget(i).removeStyleName(Styles.ACTIVE);
            }
            group.addStyleName(Styles.ACTIVE);
            clearActiveFromAnchors();
            activateAnchorFromHref(href);
        }
    }

    private void activateAnchorFromHref(String href) {
        Anchor anchor = anchorList.stream().filter(a -> {
            String[] split = a.getHref().split("#"); //$NON-NLS-1$
            boolean result = false;
            if (split.length > 1) {
                result = href.startsWith(split[1]);
            }
            return result;
        }).findFirst().orElse(null);
        if (anchor != null) {
            anchor.getParent().addStyleName(Styles.ACTIVE);
        }
    }

    private void clearActiveFromAnchors() {
        anchorList.forEach(anchor -> anchor.getParent().removeStyleName(Styles.ACTIVE));
    }
}
