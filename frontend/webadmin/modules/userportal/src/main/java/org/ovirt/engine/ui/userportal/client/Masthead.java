package org.ovirt.engine.ui.userportal.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HTML;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.common.AboutModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.common.UserPortalMode;
import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.BkgndRepeat;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.StretchImgButton;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VStack;

public class Masthead extends HLayout {
	public static final int MASTHEAD_HEIGHT = 54;
	private static final String LOGO_FILE_NAME = "user_portal_logo.gif";
	private static final String HEADER_RIGHT_IMAGE_FILE_NAME = "user_portal_header_right.jpg";
	private static final String HEADER_BACKGROUND_IMG_FILE_NAME = "bg_head.gif";
	private static StaticTextItem userName;
	private static Masthead masterHead;
	private static Event modeChangedEvent = new Event(new EventDefinition("UserPortalModeChanged", Masthead.class));
	private static UserPortalMode userPortalMode;
	private ModeSelectTab selectedTab;
	private HLayout tabsLayout;
	
	public static Masthead getInstance() {
		if (masterHead == null) {
			masterHead = new Masthead();
		}

		return masterHead;
	}

	private Masthead() {
		super();
		masterHead = this;

		GWT.log("Initializing Masthead");
		setStyleName("masterhead");

		Img logo = new Img(LOGO_FILE_NAME, 131, 60);
		logo.setStyleName("engine-masterhead-logo");

		this.setHeight(MASTHEAD_HEIGHT);


		HLayout westLayout = new HLayout();
		westLayout.setHeight(MASTHEAD_HEIGHT);
		westLayout.setWidth("50%");
		westLayout.addMember(logo);
		westLayout.setBackgroundImage(HEADER_BACKGROUND_IMG_FILE_NAME);
		westLayout.setBackgroundRepeat(BkgndRepeat.REPEAT_X);

		HLayout eastLayout = new HLayout();
		eastLayout.setAlign(Alignment.RIGHT);
		eastLayout.setBackgroundImage(HEADER_RIGHT_IMAGE_FILE_NAME);
		eastLayout.setHeight(74);
		eastLayout.setWidth(310);

		eastLayout.addMember(getModeTabSelectLayout());
		
		StaticTextItem delimiter = new StaticTextItem("delimiter");
		delimiter.setValue(" | ");
		delimiter.setShowTitle(false);
		delimiter.setTextBoxStyle("header-text-style");

		StaticTextItem userNameLabel = new StaticTextItem("userNameLabel");
		userNameLabel.setValue("User: ");
		userNameLabel.setShowTitle(false);
		userNameLabel.setTextBoxStyle("header-text-style");
		// userNameLabel.setWidth("100%");

		userName = new StaticTextItem("userName");
		userName.setShowTitle(false);
		userName.setTextBoxStyle("header-text-style");
		initLoggedInUserName();

		final DynamicForm form = new DynamicForm();
		form.setNumCols(8);
		form.setStyleName("links-style");

		final LinkItem aboutLink = new LinkItem();
		aboutLink.setLinkTitle("About");
		aboutLink.setShowTitle(false);
		aboutLink.setTextBoxStyle("header-link-style");


		aboutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final NonDraggableModalPanel winModal = new NonDraggableModalPanel(
						500, 200, "About");
				winModal.setAlign(Alignment.RIGHT);
				winModal.setAlign(VerticalAlignment.BOTTOM);
				winModal.setShowMinimizeButton(false);
				winModal.setIsModal(true);
				winModal.setScrollbarSize(0);
				winModal.centerInPage();
				winModal.addCloseClickHandler(new CloseClickHandler() {
					public void onCloseClick(CloseClientEvent event) {
						winModal.destroy();
					}
				});

				HTML aboutText = new HTML(ApplicationConstants.aboutHtmlString);

				HLayout buttonsLayout = new HLayout();
				buttonsLayout.setAlign(Alignment.RIGHT);

				Button closeButton = new Button("Close");
				buttonsLayout.addMember(closeButton);
				closeButton
						.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
							@Override
							public void onClick(
									com.smartgwt.client.widgets.events.ClickEvent event) {
								winModal.hide();
							}
						});
				winModal.addItem(aboutText);
				winModal.setFooterButtons(Alignment.RIGHT,
						closeButton);
				winModal.show();
			}
		});


		form.setItems(userNameLabel, userName, delimiter, buildSignOutLink(),
				delimiter, buildHelpLink(), delimiter, aboutLink);

		// form.setItems(delimiter, delimiter, delimiter, delimiter);

		Label productNameText = new Label(ApplicationConstants.PRODUCT_NAME);
		productNameText.setStyleName("engine-productname-label");
		productNameText.setAutoFit(true);
		productNameText.setWidth100();
		productNameText.setHeight(18);

		VStack v = new VStack();
		// v.setAutoWidth();
		v.addMember(productNameText);
		// v.setShowEdges(true);
		// v.setMembersMargin(0);
		// v.setLayoutMargin(0);
		// v.setPadding(0);
		// v.setWidth("60%");

		HStack linksStack = new HStack(10);
		linksStack.setMembers(form);
		linksStack.setHeight(17);

		// westLayout.addMember(productNameText);
		v.addMember(linksStack);
		westLayout.addMember(v);

		this.setMembers(westLayout, eastLayout);
	}

	public LinkItem buildHelpLink() {
		LinkItem helpLink = new LinkItem();
		helpLink.setLinkTitle("Guide");
		helpLink.setShowTitle(false);
		helpLink.setTextBoxStyle("header-link-style");
		helpLink.setValue(com.google.gwt.user.client.Window.Location
				.getProtocol()
				+ "//"
				+ com.google.gwt.user.client.Window.Location.getHost()
				+ "/rhev-docs/en-US/html/User_Portal_Guide/index.html");

		return helpLink;
	}

	public LinkItem buildSignOutLink() {
		LinkItem signOutLink = new LinkItem();
		signOutLink.setLinkTitle("Sign out");
		signOutLink.setShowTitle(false);

		signOutLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UserPortal.logout();
			}
		});
		signOutLink.setTextBoxStyle("header-link-style");
		return signOutLink;
	}

	class ModeSelectTab extends StretchImgButton {
		public UserPortalMode mode;
		public ModeSelectTab(final UserPortalMode mode) {
			this.mode = mode;
			setLayoutAlign(VerticalAlignment.BOTTOM);
			setSrc("buttons/modeSelectTab.png");
			setTitle(mode.title);
			setBaseStyle("modeTab");
			setHeight(25);
			setWidth(100);
			setShowHover(false);
			setShowDown(false);
			addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
				@Override
				public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
					select();
				}
			});
		}
		
		@Override
		public void select() {
			if (!selectedTab.equals(this)) {
				super.select();
				selectedTab.deselect();
				selectedTab = this;
				setUserPortalMode(mode);
			}
		}
	}
	
	public HLayout getModeTabSelectLayout() {
	    	if (tabsLayout != null)
		    return tabsLayout;
	    	
		tabsLayout = new HLayout();
		tabsLayout.setAutoWidth();
		tabsLayout.setLayoutRightMargin(20);
		
		ModeSelectTab basicModeTab = new ModeSelectTab(UserPortalMode.BASIC);
		ModeSelectTab extendedModeTab = new ModeSelectTab(UserPortalMode.EXTENDED);
		tabsLayout.addMember(basicModeTab);
		tabsLayout.addMember(extendedModeTab);

		selectedTab = basicModeTab;
		extendedModeTab.select();
//		selectedTab = extendedModeTab;
//		basicModeTab.select();
		
		return tabsLayout;
	}
	
	
	public void initLoggedInUserName() {
		if (UserPortal.getSessionUser() != null) {
			userName.setValue(UserPortal.getSessionUser().getUserName());
		} else {
			userName.setValue("");
		}
	}

	public void refreshUserName() {
		initLoggedInUserName();
		userName.redraw();
	}

	public void cleanUserName() {
		userName.setValue("");
		userName.redraw();
	}

	public static UserPortalMode getUserPortalMode() {
		return userPortalMode;
	}
	
	public void setUserPortalMode(UserPortalMode mode) {
		GWT.log("Setting user portal mode to: " + mode.name());
		this.userPortalMode = mode;
		modeChangedEvent.raise(this, EventArgs.Empty);
	}
	
	public static Event getModeChangedEvent() {
		return modeChangedEvent;
	}
}
