package org.ovirt.engine.ui.userportal.client.views.extended.maingrid;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommon.models.userportal.UserPortalListModel;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.userportal.client.Masthead;
import org.ovirt.engine.ui.userportal.client.UserPortal;
import org.ovirt.engine.ui.userportal.client.binders.ObjectNameResolver;
import org.ovirt.engine.ui.userportal.client.common.UserPortalMode;
import org.ovirt.engine.ui.userportal.client.components.GridController;
import org.ovirt.engine.ui.userportal.client.components.RefreshPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.ChangeCdModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.ItemRemoveModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewTemplateModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.NewVmModalPanel;
import org.ovirt.engine.ui.userportal.client.modalpanels.RunOnceModalPanel;
import org.ovirt.engine.ui.userportal.client.components.GridRefreshManager;
import org.ovirt.engine.ui.userportal.client.util.messages.Message;
import org.ovirt.engine.ui.userportal.client.views.extended.maingrid.components.MainGridItemSubTabs;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.KeyDownEvent;
import com.smartgwt.client.widgets.events.KeyDownHandler;
import com.smartgwt.client.widgets.layout.LayoutSpacer;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.toolbar.ToolStripMenuButton;
import com.smartgwt.client.widgets.toolbar.ToolStripSeparator;

public class MainGrid extends VLayout {
	
	UserPortalItemsGrid upItemsGrid;
	MainGridItemSubTabs upItemTabs;
	
	static private ToolStripButton runOnceButton;
	static private ToolStripButton newDesktopButton;
	static private ToolStripButton newServerButton;
	static private ToolStripButton editVmButton;
	static private ToolStripButton removeVmButton;
	static private ToolStripButton changeCdButton;
	static private ToolStripButton newTemplateButton;

	public UserPortalListModel uplm = new UserPortalListModel();
	
	private MainGrid instance = this;
	
	public MainGrid() {
		
		upItemsGrid = new UserPortalItemsGrid(this);
		upItemsGrid.setModel(uplm);
		
		//upItemTabs = new UserPortalItemDetailTabPane(this);
		upItemTabs = new MainGridItemSubTabs(uplm, upItemsGrid);
		setOverflow(Overflow.HIDDEN);
		setWidth100();
		setHeight100();

		// Add view's members
		addMember(getToolbar());
		addMember(upItemsGrid.getLayout());
		addMember(upItemTabs);
		
		// At the initial item selection in UserPortalListModel, the detail models are initialized 
		// and the detail tab section should become visible and the resize bar should be visible in the items grid
		uplm.getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs pcea = (PropertyChangedEventArgs)args;
				if (pcea.PropertyName.equals("DetailModels")) {
					//upItemTabs.initDetailsPane();
					initItemSelected();
				}
			}
		});

		upItemsGrid.getSelectionStatusChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				if (args == GridController.ITEM_SELECTED_EVENT_ARGS) {
					upItemsGrid.getLayout().setShowResizeBar(true);
					upItemTabs.show();
				}
				else {
					upItemsGrid.getLayout().setShowResizeBar(false);
					upItemTabs.hide();
				}
				
			}
		});
		
		upItemsGrid.getItemsChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
			    if (Masthead.getUserPortalMode() == UserPortalMode.EXTENDED && 
				UserPortal.getSessionConnectAutomatically() && uplm.getCanConnectAutomatically())
		    	    {
				UserPortalItemModel userPortalItemModel = uplm.GetStatusUpVms(uplm.getItems()).get(0);
				
				if (userPortalItemModel != null)
				{
				    userPortalItemModel.getDefaultConsole().getConnectCommand().Execute();
				    UserPortal.setSessionConnectAutomatically(false);
				}				
		    	    }
			    upItemsGrid.getItemsChangedEvent().removeListener(this);
			}
		});

		addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				upItemsGrid.keyPressed(EventHandler.getKey());
			}
		});

		setButtonsAvailabilityListeners();
		
	}

	public ToolStrip getToolbar() {
		ToolStrip toolBar = new ToolStrip();
		toolBar.setWidth100();
		toolBar.setBackgroundColor("#FFFFFF");
		toolBar.setStyleName("mainGrid-Toolbar");
		
		newDesktopButton = new ToolStripButton("New Desktop");
		newDesktopButton.setAutoFit(true);
		newDesktopButton.addClickHandler(newDesktopButtonClickHandler);
		newDesktopButton.setHeight(19);

		newServerButton = new ToolStripButton("New Server");
		newServerButton.setAutoFit(true);
		newServerButton.addClickHandler(newServerButtonClickHandler);
		newServerButton.setHeight(19);
		
		editVmButton = new ToolStripButton("Edit");
		editVmButton.setAutoFit(true);
		editVmButton.addClickHandler(editVmButtonClickHandler);
		editVmButton.setDisabled(true);
		editVmButton.setHeight(19);
		
		removeVmButton = new ToolStripButton("Remove");
		removeVmButton.setAutoFit(true);
		removeVmButton.addClickHandler(removeVmButtonClickHandler);
		removeVmButton.setDisabled(true);
		removeVmButton.setHeight(19);
		
		runOnceButton = new ToolStripButton("Run Once");
		runOnceButton.setAutoFit(true);
		runOnceButton.addClickHandler(runOnceButtonClickHandler);
		runOnceButton.setDisabled(true);
		runOnceButton.setHeight(19);
		
		changeCdButton = new ToolStripButton("Change CD");
		changeCdButton.setAutoFit(true);
		changeCdButton.addClickHandler(changeCdButtonClickHandler);
		changeCdButton.setDisabled(true);
		changeCdButton.setHeight(19);
		
		newTemplateButton = new ToolStripButton("Make Template");
		newTemplateButton.setAutoFit(true);
		newTemplateButton.addClickHandler(newTemplateButtonClickHandler);
		newTemplateButton.setDisabled(true);
		newTemplateButton.setHeight(19);

		toolBar.setMembers(newServerButton, new ToolStripSeparator(), newDesktopButton, new ToolStripSeparator(), editVmButton, new ToolStripSeparator(), removeVmButton, new ToolStripSeparator(), runOnceButton, new ToolStripSeparator(), changeCdButton, new ToolStripSeparator(), newTemplateButton, new LayoutSpacer(), new RefreshPanel(upItemsGrid));
		
		return toolBar;
	}
	
	private ToolStripMenuButton getDebugMenuButton() {
		Menu menu = new Menu();
		menu.setShowShadow(true);
		menu.setShadowDepth(3);
		MenuItem infoMsgItem = new MenuItem("Send info msg", "msg/msg_info.png");
		infoMsgItem
				.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
					@Override
					public void onClick(MenuItemClickEvent event) {
						UserPortal.getMessageCenter().notify(
								new Message("Added info msg!",
										Message.Severity.Info));
					}
				});
		MenuItem warnMsgItem = new MenuItem("Send warn msg", "msg/msg_warn.png");
		warnMsgItem
				.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
					@Override
					public void onClick(MenuItemClickEvent event) {
						UserPortal.getMessageCenter().notify(
								new Message("Added info msg!",
										Message.Severity.Warning));
					}
				});
		MenuItem errorMsgItem = new MenuItem("Handle error",
				"msg/msg_error.png");
		errorMsgItem
				.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {
					@Override
					public void onClick(MenuItemClickEvent event) {
						UserPortal.getErrorHandler().handleError(
								"An unexpected exception!",
								new Exception("Detailed exception"));
					}
				});

		menu.setItems(infoMsgItem, warnMsgItem, errorMsgItem);

		ToolStripMenuButton menuButton = new ToolStripMenuButton("Debug", menu);
		menuButton.setWidth(100);
		return menuButton;
	}

	ClickHandler newDesktopButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getNewDesktopCommand().Execute();
			Window newVmModalPanel = new NewVmModalPanel(upItemsGrid, uplm.getVmModel(), uplm);
			newVmModalPanel.draw();
		}
	};
	
	ClickHandler newServerButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getNewServerCommand().Execute();
			Window newVmModalPanel = new NewVmModalPanel(upItemsGrid, uplm.getVmModel(), uplm);
			newVmModalPanel.draw();
		}
	};
	
	ClickHandler editVmButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getEditCommand().Execute();
			Window newVmModalPanel = new NewVmModalPanel(upItemsGrid, uplm.getVmModel(), uplm);
			newVmModalPanel.draw();
		}
	};

	ClickHandler removeVmButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getRemoveCommand().Execute();
			Window removeVmModalPanel = new ItemRemoveModalPanel("Remove Virtual Machine(s)", "Virtual Machine(s)", uplm, new ObjectNameResolver() {
				
				@Override
				public String getItemName(Object o) {
					return ((VM)((UserPortalItemModel)o).getEntity()).getvm_name();
				}
			}, upItemsGrid);
			removeVmModalPanel.draw();
		}
	};

	ClickHandler runOnceButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getRunOnceCommand().Execute();
			Window runOnceModalPanel = new RunOnceModalPanel(instance);
			runOnceModalPanel.draw();
		}
	};
	
	ClickHandler changeCdButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getChangeCdCommand().Execute();
			Window attachCDModalPanel = new ChangeCdModalPanel(uplm.getAttachCdModel().getTitle(), instance);
			attachCDModalPanel.draw();
		}
	};
	
	ClickHandler newTemplateButtonClickHandler = new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			uplm.getNewTemplateCommand().Execute();
			Window newTemplateModalPanel = new NewTemplateModalPanel(uplm.getVmModel().getTitle(), instance);
			newTemplateModalPanel.draw();
		}
	};
		
	public void setButtonsAvailabilityListeners() {
		uplm.getNewDesktopCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					newDesktopButton.setDisabled(!uplm.getNewDesktopCommand().getIsExecutionAllowed());
				}
			}
		});
		
		uplm.getNewServerCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					newServerButton.setDisabled(!uplm.getNewServerCommand().getIsExecutionAllowed());
				}
			}
		});
		
		uplm.getRemoveCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					removeVmButton.setDisabled(!uplm.getRemoveCommand().getIsExecutionAllowed());
				}
			}
		});
		uplm.getEditCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					editVmButton.setDisabled(!uplm.getEditCommand().getIsExecutionAllowed());
				}
			}
		});
		uplm.getRunOnceCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					runOnceButton.setDisabled(!uplm.getRunOnceCommand().getIsExecutionAllowed());
				}
			}
		});
		
		uplm.getChangeCdCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					changeCdButton.setDisabled(!uplm.getChangeCdCommand().getIsExecutionAllowed());
				}
			}
		});
		
		uplm.getNewTemplateCommand().getPropertyChangedEvent().addListener(new IEventListener() {
			@Override
			public void eventRaised(Event ev, Object sender, EventArgs args) {
				PropertyChangedEventArgs evArgs = (PropertyChangedEventArgs)args;
				if (evArgs.PropertyName.equals("IsExecutionAllowed")) {
					newTemplateButton.setDisabled(!uplm.getNewTemplateCommand().getIsExecutionAllowed());
				}
			}
		});
	}

	public void initItemSelected() {
		removeVmButton.setDisabled(!uplm.getRemoveCommand().getIsExecutionAllowed());
		editVmButton.setDisabled(!uplm.getEditCommand().getIsExecutionAllowed());
		runOnceButton.setDisabled(!uplm.getRunOnceCommand().getIsExecutionAllowed());
		changeCdButton.setDisabled(!uplm.getChangeCdCommand().getIsExecutionAllowed());
		newTemplateButton.setDisabled(!uplm.getChangeCdCommand().getIsExecutionAllowed());
	}
	
	@Override
    public void show() {
        super.show();
        GridRefreshManager.getInstance().subscribe(upItemsGrid);
    }
    
   @Override
    public void hide() {
        super.hide();
        GridRefreshManager.getInstance().unsubscribe(upItemsGrid);
    }
	
	// Should be called whenever an action is performed on the grid which requires a refresh or a series of refreshes
	public void gridActionPerformed() {
		upItemsGrid.gridChangePerformed();
	}
}
