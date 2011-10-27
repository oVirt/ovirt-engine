package org.ovirt.engine.ui.userportal.client.components;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;
import com.smartgwt.client.types.ImageStyle;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.layout.HLayout;


public class RefreshPanel extends HLayout {
    
    private GridController<?> gridController;    
    private DecoratedPopupPanel menuPopup;
    private RefreshRateOptionsMenu refreshOptionsMenu;
    private ToggleButton refreshMenuButton;
    private PushButton refreshButton;
    
    public RefreshPanel(GridController<?> gridController) {        
        this.gridController = gridController;
        
        // Set panel's attributes
        setStyleName("refreshPanel");        
        setAlign(VerticalAlignment.CENTER);
        setWidth(32);
        setHeight(18);        
               
        // Create menu popup        
        menuPopup = new DecoratedPopupPanel(true, false);
        menuPopup.setStyleName("refreshRateMenuPopup");
        
        // Create refresh and refresh menu buttons
        createRefreshButton();
        createRefreshMenuButton();
                                 
        // Create refresh options menu
        refreshOptionsMenu = getRefreshOptionsMenu();
                
        // Add menu to the popup
        menuPopup.add(refreshOptionsMenu);
        menuPopup.addAutoHidePartner(refreshMenuButton.getElement());
        menuPopup.addCloseHandler(new CloseHandler<PopupPanel>(){
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {                
                refreshMenuButton.setDown(false);
            }});
        
        // Hide popup on window resize
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event)
            {
                menuPopup.hide();
            }
         });

        // Add mouse hover events
        addMouseOverHandler(new com.smartgwt.client.widgets.events.MouseOverHandler(){
            public void onMouseOver(com.smartgwt.client.widgets.events.MouseOverEvent event)
            {
                setStyleName("refreshPanel-over");
            }
        });
        addMouseOutHandler(new com.smartgwt.client.widgets.events.MouseOutHandler(){
            public void onMouseOut(com.smartgwt.client.widgets.events.MouseOutEvent event)
            {
                setStyleName("refreshPanel");
            }
        });
        
        // Create panel separator
        Img separatorImg = new Img("general/separator.gif", 3, 16);
        separatorImg.setImageWidth(1);   
        separatorImg.setImageHeight(9);
        separatorImg.setImageType(ImageStyle.CENTER);
        
        // Add refresh button and refresh options menu button
        addMember(refreshButton);
        addMember(separatorImg);
        addMember(refreshMenuButton);  
    }
    
    private void createRefreshButton() {          
        Image refreshIcon = new Image("images/general/refresh_button.png");
        refreshIcon.setPixelSize(11, 11);
        refreshIcon.setStyleName("refreshIcon");
        
        refreshButton = new PushButton(refreshIcon);        
        refreshButton.setStyleName("refreshButton");        
        refreshButton.setPixelSize(17, 17);
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                gridController.search(); 
              }
        });
    }

    private void createRefreshMenuButton() {
        Image refreshMenuIcon = new Image("images/general/triangle_down.gif");
        refreshMenuIcon.setPixelSize(7, 4);
        refreshMenuIcon.setStyleName("refreshMenuIcon");
        
        refreshMenuButton = new ToggleButton(refreshMenuIcon);        
        refreshMenuButton.setStyleName("refreshMenuButton");
        refreshMenuButton.setPixelSize(13, 17);
        refreshMenuButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // Show/Hide popup
                if (refreshMenuButton.isDown()) {                   
                    // Check selected item     
                    int globalRefreshRate = GridRefreshManager.getInstance().getGlobalRefreshRate();                   
                    refreshOptionsMenu.selectItem(refreshOptionsMenu.getItemByRefreshRate(globalRefreshRate));                                        
                    menuPopup.showRelativeTo(refreshMenuButton);
                }
                else {
                    menuPopup.hide();
                }
              }
        });
    }

    private RefreshRateOptionsMenu getRefreshOptionsMenu() {
        // Create refresh rate options menu bar
        RefreshRateOptionsMenu refreshOptionsMenu = new RefreshRateOptionsMenu(true);
        refreshOptionsMenu.setStyleName("refreshRateMenu");            
        
        // Create menu's title and add it as the first item
        MenuItem title = new MenuItem("Refresh Rate", new Command() { public void execute() {} });
        title.setEnabled(false);
        title.setStylePrimaryName("refreshMenuTitle");        
        refreshOptionsMenu.addItem(title);      
        
        for (int itemIndex = 0; itemIndex < GridRefreshManager.REFRESH_RATES.length; itemIndex++) { 
            // Create command and menu item
            RefreshRateOptionCommand command = new RefreshRateOptionCommand();            
            RefreshRateOptionMenuItem refreshRateOption = new RefreshRateOptionMenuItem(GridRefreshManager.REFRESH_RATES[itemIndex], command);
            refreshRateOption.setStyleName("refreshRateOption");   
            
            // Set command with the item it's associated with
            command.setMenuItem(refreshRateOption);
            
            // Add item (and a separator if needed)
            refreshOptionsMenu.addItem(refreshRateOption);
            if (itemIndex != GridRefreshManager.REFRESH_RATES.length-1)  refreshOptionsMenu.addSeparator();
        }
        
        return refreshOptionsMenu;
    }   
  
    // A custom menu bar for 'RefreshRateOptionMenuItem' items.
    public class RefreshRateOptionsMenu extends MenuBar {
        public RefreshRateOptionsMenu(boolean vertical) {
            super(vertical);
        }

        public void selectItem(RefreshRateOptionMenuItem menuItem) {
            
            if (menuItem == null) return;
            
            // Select the specified item after unselecting the others 
            for (MenuItem item : this.getItems()) {
                if (item instanceof RefreshRateOptionMenuItem) {
                    RefreshRateOptionMenuItem refreshRateOptionMenuItem = (RefreshRateOptionMenuItem)item;
                    refreshRateOptionMenuItem.unselect();
                    refreshRateOptionMenuItem.setSelectionStyle(false);
                }
            }
            
            menuItem.select();
        }
        
        public RefreshRateOptionMenuItem getItemByRefreshRate(int refreshRate) {
            // Find the menu item that is associated with the specified refresh rate value
            for (MenuItem item : this.getItems()) {
                if (item instanceof RefreshRateOptionMenuItem) {                    
                    RefreshRateOptionMenuItem refreshRateOptionMenuItem = (RefreshRateOptionMenuItem)item;
                    if (refreshRateOptionMenuItem.getRefreshRate() == refreshRate)                     
                        return refreshRateOptionMenuItem;      
                }
            }
            
            return null;
        }
    }
    
    // A custom menu bar with check icon
    public class RefreshRateOptionMenuItem extends MenuItem {
        
        private Image checkItem;
        private int refreshRate;
                
        public RefreshRateOptionMenuItem(int refreshRate, Command cmd) {
            super("", true, cmd);                   
           
            this.refreshRate = refreshRate;
            
            checkItem = new Image("images/general/check_icon.png");
            checkItem.setStyleName("checkItem");
            checkItem.setVisible(false); 
            getElement().appendChild(checkItem.getElement());                
            
            Anchor textItem = new Anchor();
            textItem.setText(String.valueOf(refreshRate) + " sec");
            textItem.setStyleName("textItem");
            getElement().appendChild(textItem.getElement());
        }
        
        public void select() {
            checkItem.setVisible(true);   
        }
        
        public void unselect() {
            checkItem.setVisible(false);   
        }
        
        public int getRefreshRate() {
            return refreshRate;
        }
        
        public void setSelectionStyle(boolean selected) {
            super.setSelectionStyle(selected);
        }
    }
    
    public class RefreshRateOptionCommand implements Command {
        
        private RefreshRateOptionMenuItem menuItem;

        public RefreshRateOptionCommand() {
            super();
        }
        
        public RefreshRateOptionMenuItem getMenuItem() {
            return menuItem;
        }

        public void setMenuItem(RefreshRateOptionMenuItem menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public void execute() {            
            menuPopup.hide();
            refreshMenuButton.setDown(false);
            GridRefreshManager.getInstance().setGlobalRefreshRate(menuItem.getRefreshRate());    
        }
    }
}
