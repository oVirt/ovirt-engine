package org.ovirt.engine.ui.userportal.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.FrontendFailureEventArgs;
import org.ovirt.engine.ui.uicommon.TypeResolver;
import org.ovirt.engine.ui.uicommon.models.common.AboutModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalLoginModel;
import org.ovirt.engine.ui.uicompat.IAsyncCallback;
import org.ovirt.engine.ui.userportal.client.common.Severity;
import org.ovirt.engine.ui.userportal.client.common.UserPortalMode;
import org.ovirt.engine.ui.userportal.client.components.UserPortalTimerFactory;
import org.ovirt.engine.ui.userportal.client.modalpanels.MessageDialog;
import org.ovirt.engine.ui.userportal.client.uicommonext.FrontendEventsHandlerImpl;
import org.ovirt.engine.ui.userportal.client.uicommonext.UiCommonDefaultTypeResolver;
import org.ovirt.engine.ui.userportal.client.util.ErrorHandler;
import org.ovirt.engine.ui.userportal.client.util.messages.MessageCenter;
import org.ovirt.engine.ui.userportal.client.views.LoginView;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.core.KeyIdentifier;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.KeyCallback;
import com.smartgwt.client.util.Page;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class UserPortal implements EntryPoint {
	private static Logger logger = Logger.getLogger(UserPortal.class.getName());

	private static final int NORTH_HEIGHT = 90; // MASTHEAD_HEIGHT +
												// APPLICATION_MENU_HEIGHT +
												// Some space
	private VLayout mainLayout;
	private HLayout northLayout;
	// private VLayout eastLayout;
	private HLayout eastLayout;
	// private VLayout westLayout;
	private HLayout southLayout;
	private HLayout footerLayout;

	static boolean signoutInProcess = false;
	private static VdcUser sessionUser;
	private static boolean sessionConnectAutomatically;
	private static Timer sessionTimer = UserPortalTimerFactory.factoryTimer("Session Timer", new Timer() {
		@Override
		public void run() {
			GWT.log("Session Timer Expired");
			UserPortalTimerFactory.cancelAllTimers();
			new LoginView().showLoginView(); // log user out, show login dialog
		}
	});

	private static UserPortal userPortal;
	private static Canvas content;
	private RootCanvas rootCanvas;
	private MessageDialog errorDialog;
	
	private static boolean isENGINEUser;
	private static UserPortalLoginModel uplm;
	private static IEventListener updateIsENGINEUserEventListener;

	// Messages
	private static ErrorHandler errorHandler = new ErrorHandler();
	private static MessageCenter messageCenter;

	private static String productVersion;

	// private static Messages messages;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		logger.finer("Starting to load UserPortal module.");

		userPortal = this;

		Document.get().setTitle(ApplicationConstants.APPLICATION_WEB_TITLE);

		if (!GWT.isScript()) {
			KeyIdentifier debugKey = new KeyIdentifier();
			debugKey.setCtrlKey(true);
			debugKey.setKeyName("D");
			Page.registerKey(debugKey, new KeyCallback() {
				public void execute(String keyName) {
					SC.showConsole();
				}
			});
		}

		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable e) {
				logger.log(Level.SEVERE, "Unexpected error: ", e);
				getErrorHandler()
						.handleError("An unexpected error occurd: ", e);
			}
		});

		Frontend.getFrontendFailureEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
                FrontendFailureEventArgs failureArgs = (FrontendFailureEventArgs) args;
                // dispose existing dialog
                if (errorDialog != null) {
                    errorDialog.closeClick();
                    errorDialog = null;
                }
                if (failureArgs.getMessage() != null) {
                    errorDialog = new MessageDialog("Operation Cancelled", failureArgs.getMessage(), Severity.ERROR);
                    errorDialog.show();
                } else if (failureArgs.getMessages() != null) {
                    errorDialog = new MessageDialog("Operation Cancelled", failureArgs.getMessages(), Severity.ERROR);
                    errorDialog.show();
                }

                UserPortalTimerFactory.cancelAllTimers();
            }
		});

		Frontend.getFrontendNotLoggedInEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				if (!signoutInProcess) {
					GWT.log("Signing out...");
					signoutInProcess = true;
					logout();
				}
			}
		});

		messageCenter = new MessageCenter();
		// messages = GWT.create(Messages.class);

		initUiCommon();

		AboutModel aboutModel = new AboutModel();
		aboutModel.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				if (((PropertyChangedEventArgs)args).PropertyName.equals("ProductVersion")) {
					productVersion = ((AboutModel)sender).getProductVersion();
				}
			}
		});

		createIsENGINEUserEventListener();
		checkLoginStatus();
	}

	public static void createIsENGINEUserEventListener() {
	    uplm = new UserPortalLoginModel();
	    updateIsENGINEUserEventListener = new IEventListener() {
		@Override
		public void eventRaised(Event ev, Object sender, EventArgs args) {						    
		    Object isENGINEUserObj = uplm.getIsENGINEUser().getEntity();
		    if (isENGINEUserObj != null)
		    {
			Boolean isENGINEUser = (Boolean)isENGINEUserObj;
			Masthead.getInstance().setUserPortalMode(isENGINEUser ? UserPortalMode.BASIC : UserPortalMode.EXTENDED);
			Masthead.getInstance().getModeTabSelectLayout().setVisible(isENGINEUser ? false : true);
								    
			userPortal.constructUserPortalUI();
			
			uplm.getIsENGINEUser().getPropertyChangedEvent().removeListener(this);
		    }    		    
		}
	    };
	}
	
	
	public static void checkLoginStatus() {
		logger.finer("Checking auth status...");

		Frontend.getLoggedInUser(new IAsyncCallback<VdcUser>() {
			@Override
			public void OnSuccess(VdcUser result) {
				if (result == null) {
					//TODO: The view itself need to take care of cleanups
					//Clean previous content if there's any
					/*
					Masthead.getInstance().refreshUserName();
					if (userPortal != null && userPortal.eastLayout != null) {
						ContextArea contextArea = (ContextArea) userPortal.eastLayout;
						contextArea.mainGrid.upItemsGrid.hide();
					}*/
					if (userPortal.rootCanvas != null)
						userPortal.rootCanvas.clear();
					new LoginView().showLoginView();
				} else {
					logger.finer("User is already logged in, constructing UI.");
					setSessionUser(result);
					Frontend.setLoggedInUser(result);
					signoutInProcess = false;
					if (!uplm.getIsENGINEUser().getPropertyChangedEvent().getListeners().contains(updateIsENGINEUserEventListener))
						uplm.getIsENGINEUser().getPropertyChangedEvent().addListener(updateIsENGINEUserEventListener);
					uplm.UpdateIsENGINEUser(result);
				}
			}

			@Override
			public void OnFailure(VdcUser result) {
				logger.finer("User is NOT logged in, generating login view.");
				new LoginView().showLoginView();
				signoutInProcess = false;
			}
		});
	}

	// Session methods
	public static void resetSessionTimer() {
		logger.finer("Refreshing Session Timer");
		sessionTimer.schedule(30 * 60 * 1000); // http session timeout after 30
												// minutes.
	}

	public void constructUserPortalUI() {
		rootCanvas = new RootCanvas();
		rootCanvas.setOverflow(Overflow.HIDDEN);

		// initialize the main layout container
		mainLayout = new VLayout();
		mainLayout.setWidth100();
		mainLayout.setHeight100();

		// initialize the North layout container
		northLayout = new HLayout();
		northLayout.setHeight(NORTH_HEIGHT);

		VLayout vLayout = new VLayout();
		// add the Masthead to the nested layout container
		vLayout.addMember(Masthead.getInstance());
		//Instance is already available from first layout construction, reinitalization of the logged user must occur
		//TODO: The masterhead view should be refreshed and take care of its childs
		Masthead.getInstance().initLoggedInUserName();
		// add the Application menu to the nested layout container
		// vLayout.addMember(new ApplicationMenu());
		// add the nestd layout container to the North layout container
		northLayout.addMember(vLayout);

		// initialize the West layout container
		// westLayout = new NavigationPane();
		// initialize the East layout container
		eastLayout = new ContextArea();

		// initialize the South layout container
		southLayout = new HLayout();

		// set the Navigation Pane and ContextArea as members of the South
		// layout container
		// southLayout.setMembers(westLayout, eastLayout);
		southLayout.setMembers(eastLayout);

		// initialize the Footer layout container
		footerLayout = new Footer();

		// add the North and South, Footer layout containers to the main
		// layout
		// container
		mainLayout.addMember(northLayout);
		mainLayout.addMember(southLayout);
		mainLayout.addMember(footerLayout);

		rootCanvas.addMember(mainLayout);
		rootCanvas.draw();
		//		} else {
		//			// UI Already built from previous login, just refresh grid
		//			Masthead.getInstance().refreshUserName();
		//			ContextArea contextArea = (ContextArea) eastLayout;
		//			contextArea.mainGrid.upItemsGrid.invalidateCache();
		//			contextArea.mainGrid.upItemsGrid.show();
		//		}
	}

	// --Static utils--
	public static MessageCenter getMessageCenter() {
		return messageCenter;
	}

	public static ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public static VdcUser getSessionUser() {
		return sessionUser;
	}

	public static void setSessionUser(VdcUser user) {
		UserPortal.sessionUser = user;
	}
	

	public static void setIsENGINEUser(boolean isENGINEUser) {
		UserPortal.isENGINEUser = isENGINEUser;
	}

	public static boolean getSessionConnectAutomatically() {
	    return sessionConnectAutomatically;
	}
	
	public static void setSessionConnectAutomatically(boolean sessionConnectAutomatically) {
	    UserPortal.sessionConnectAutomatically = sessionConnectAutomatically;
	}

	// public static Messages getMessages() {
	// return messages;
	// }

	public static void setContent(Canvas newContent) {
		Canvas contentCanvas = Canvas.getById("ROOT");

		for (Canvas child : contentCanvas.getChildren()) {
			child.destroy();
		}

		if (contentCanvas != null) {
			content = newContent;
			contentCanvas.addChild(newContent);
		}

		contentCanvas.markForRedraw();
	}

	public class RootCanvas extends VLayout {
		Canvas currentCanvas;

		private RootCanvas() {
			setWidth100();
			setHeight100();
		}

		public void renderView(Canvas view) {
			setContent(view);
		}
	}

	public static void logout() {
		UserPortalTimerFactory.cancelAllTimers();
		if (getSessionUser() != null) {
			Frontend.Logoff(getSessionUser(),
					new AsyncCallback<VdcReturnValueBase>() {
						@Override
						public void onSuccess(VdcReturnValueBase arg0) {
							setSessionUser(null);
							Frontend.setLoggedInUser(null);
							// reload page to close any existing popups (#703353, #707958)
							Window.Location.reload();
						}

						@Override
						public void onFailure(Throwable caught) {
							UserPortal.getErrorHandler().handleError(
									"Failed to logout", caught);
							signoutInProcess = false;
						}
					});
		}
	}

	public static String getProductVersion() {
		return productVersion;
	}

	private void initUiCommon() {
		UiCommonDefaultTypeResolver resolverImpl = new UiCommonDefaultTypeResolver();
		TypeResolver.Initialize(resolverImpl);
		FrontendEventsHandlerImpl fehi = new FrontendEventsHandlerImpl();
		Frontend.initEventsHandler(fehi);
	}
}