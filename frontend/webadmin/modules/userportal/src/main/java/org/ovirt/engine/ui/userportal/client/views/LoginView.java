package org.ovirt.engine.ui.userportal.client.views;

import java.util.Date;
import java.util.logging.Logger;

import com.google.gwt.user.client.Cookies;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.userportal.client.ApplicationConstants;
import org.ovirt.engine.ui.userportal.client.UserPortal;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameIdResolver;
import org.ovirt.engine.ui.userportal.client.components.CheckboxItemModelBinded;
import org.ovirt.engine.ui.userportal.client.components.UPLabel;
import org.ovirt.engine.ui.userportal.client.components.UPTextItem;
import org.ovirt.engine.ui.userportal.client.components.SelectBoxListModelBinded;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.BkgndRepeat;
import com.smartgwt.client.types.FormErrorOrientation;
import com.smartgwt.client.types.Positioning;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.SubmitValuesEvent;
import com.smartgwt.client.widgets.form.events.SubmitValuesHandler;
import com.smartgwt.client.widgets.form.fields.HeaderItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.SubmitItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class LoginView extends Window {
	private static Logger logger = Logger.getLogger(LoginView.class.getName());

	private static final String LOGIN_AUTOCONNECT_COOKIE_NAME = "Login_ConnectAutomaticallyChecked";

	public LoginView() {
		setAttribute("edgeTop", 14, false);
		setAttribute("edgeBottom", 14, false);
		setShowTitle(false);
		setShowHeader(false);
		setEdgeOffset(4);
		setMargin(-2);
		setLayoutAlign(Alignment.LEFT);
		setDefaultLayoutAlign(Alignment.LEFT);
		HLayout headerLayout = new HLayout();
		headerLayout.setWidth100();
		headerLayout.setHeight(44);

		HLayout headerLeft = new HLayout();
		headerLeft.addMember(new Img("login_page_header_logo.png", 51, 44));

		VLayout headerCenter = new VLayout();
		headerCenter.setWidth100();
		headerCenter.setHeight(44);
		headerCenter.setBackgroundPosition("3");
		headerCenter.setBackgroundImage("login_page_header_background.png");
		headerCenter.setBackgroundRepeat(BkgndRepeat.REPEAT_X);

		UPLabel headerTitle = new UPLabel("login-panel-header-title");
		headerTitle.setContents(ApplicationConstants.PRODUCT_NAME);
		headerCenter.setAlign(VerticalAlignment.CENTER);
		headerCenter.addMember(headerTitle);

		HLayout headerRight = new HLayout();
		headerRight.setAlign(Alignment.RIGHT);
		headerRight.addMember(new Img("login_page_header_image.png", 133, 44));

		headerLayout.setMembers(headerLeft, headerCenter, headerRight);
		headerLayout.setStyleName("login-panel-header");
		addItem(headerLayout);

		setEdgeImage("login_panel_edge.png");
		setEdgeSize(14);
		setShowEdges(true);
		setWidth(440);
		setShowMinimizeButton(false);
		setShowCloseButton(false);
		setHeight(280);
		setIsModal(true);
		setShowModalMask(true);
		setCanDragResize(false);
		setCanDragReposition(false);
		centerInPage();
	}
	
    @Override
    protected void onDraw() {
        // get body auto-child
        Canvas body = getById(getID() + "_body");
        // disable SmartClient custom scrollbars (#656384, #698988)
        // this is required because the custom scrollbars feature catches arrow key events also inside text fields.
        body.setShowCustomScrollbars(false);
    }

	private DynamicForm form;
	private SubmitItem loginButton;
	private boolean currentlyDispalyed = false;
	static UserPortalLoginModel uplm = null;

	private final int FORM_ITEMS_SIZE_PX = 180;
	public void showLoginView() {
		LoginView.uplm = new UserPortalLoginModel();
		currentlyDispalyed = true;

		form = new DynamicForm();
		form.setID("LoginForm");
		form.setHeight100();
		form.setWidth100();
		form.setPadding(5);
		form.setMargin(5);
		form.setCellPadding(4);
		form.setShowErrorText(true);
		form.setErrorOrientation(FormErrorOrientation.BOTTOM);

		HeaderItem header = new HeaderItem();
		header.setValue("oVirt Enterprise Virtualization Engine User Portal Login!");

		final UPTextItem user = new UPTextItem("user", "<nobr>User Name</nobr>");
		user.setWidth(FORM_ITEMS_SIZE_PX);
		user.setTitleAlign(Alignment.LEFT);
		user.setRequired(true);
		
		// Needed for user names that specify FQDN using @, thus we need to set the name in the model every change
		user.addChangedHandler(new ChangedHandler() {
			@Override
			public void onChanged(ChangedEvent event) {
				uplm.getUserName().setEntity(user.getValue());
			}
		});
		//user.setAttribute("autoComplete", "native");
		
		final PasswordItem password = new PasswordItem("password", "Password");
		password.setWidth(FORM_ITEMS_SIZE_PX);
		password.setTitleAlign(Alignment.LEFT);
		password.setRequired(true);
		//password.setAttribute("autoComplete", "native");

		final SelectBoxListModelBinded domain = new SelectBoxListModelBinded("Domain", uplm.getDomain(), new ObjectNameIdResolver() {
			@Override
			public String getItemName(Object o) {
				return (String)o;
			}
			
			@Override
			public String getItemId(Object o) {
				return (String)o;
			}
		});

		domain.setWidth(FORM_ITEMS_SIZE_PX);
		domain.setTitleAlign(Alignment.LEFT);
		domain.setRequired(true);
		
		form.setAlign(Alignment.CENTER);
		
		// Create 'Connect Automatically' checkbox and set its value by the saved cookie.
		CheckboxItemModelBinded autoConnectCheckbox = new CheckboxItemModelBinded("Connect Automatically", uplm.getIsAutoConnect());
		autoConnectCheckbox.setColSpan(2);
		autoConnectCheckbox.setShowTitle(false);
		uplm.getIsAutoConnect().setEntity(readConnectAutomaticallyCookie());

		loginButton = new SubmitItem("login", "Login");
		loginButton.setColSpan(3);
		loginButton.setAlign(Alignment.RIGHT);
		
		form.setFields(user, password, domain, autoConnectCheckbox, loginButton);

		form.setAutoWidth();
		form.setAutoHeight();
        form.setSaveOnEnter(true);
		
		// Wrap the form in a layout in order to set it's layout and positioning
		HLayout h = new HLayout();
		h.setWidth100();
		h.setAutoHeight();
		h.addMember(form);
		h.setAlign(Alignment.CENTER);
		h.setPosition(Positioning.RELATIVE);

		addItem(h);
		show();
		form.focusInItem(user);

		form.addSubmitValuesHandler(new SubmitValuesHandler() {
			public void onSubmitValues(SubmitValuesEvent submitValuesEvent) {
				if (form.validate()) {
                    login((String) user.getValue(), (String) password.getValue(), (String) uplm.getDomain()
                            .getSelectedItem());
				}
			}
		});
	}
	
	// Save 'IsAutoConnect' value to a cookie and set expire date to fifty years from now
	private void saveConnectAutomaticallyCookie()
	{       
	    long expire = new Date().getTime() + (1000 * 60 * 60 * 24 * 365 * 50); // fifty years       
	    String cookieValue = uplm.getIsAutoConnect().getEntity().toString();
	       
	    Cookies.setCookie(LOGIN_AUTOCONNECT_COOKIE_NAME, cookieValue, new Date(expire));    
	}
	   
	// Return 'IsAutoConnecvdt' value from the cookie - if exists; Otherwise, true 
	// (connect automatically is true by default). 
	private Boolean readConnectAutomaticallyCookie()
	{       
	    String connectAutomatically = Cookies.getCookie(LOGIN_AUTOCONNECT_COOKIE_NAME);
	    if (connectAutomatically != null) return new Boolean(connectAutomatically);
	    else return true;
	}

	private void login(final String user, final String password,
			final String domain) {
		logger.finer("Logging user [" + user + "] to domain [" + domain + "].");
		uplm.getUserName().setEntity(user);
		uplm.getPassword().setEntity(password);
		uplm.getDomain().setSelectedItem(domain);

		uplm.getLoggedInEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				// Success login
				logger.info("Successfully logged in!");
				destroy();
				currentlyDispalyed = false;
				//TODO: Waiting for UiCommon to support getLoggedUser
				UserPortal.setSessionUser(uplm.getLoggedUser());
				UserPortal.setSessionConnectAutomatically(((Boolean)uplm.getIsAutoConnect().getEntity()).booleanValue());
				saveConnectAutomaticallyCookie();
				Frontend.setLoggedInUser(uplm.getLoggedUser());
				UserPortal.checkLoginStatus();
			}
		});
		
		uplm.getLoginFailedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				// Success login
				logger.warning("User failed to login!");
				form.setFieldErrors(
						"login",
						uplm.getMessage(),
						true);
				
				// Clears password field
				form.getField("password").setValue("");				
			}
		});

		uplm.Login();
	}

}
