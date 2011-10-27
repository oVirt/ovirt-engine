package org.ovirt.engine.ui.userportal.client.components;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.ui.userportal.client.common.Severity;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.BkgndRepeat;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class NonDraggableModalPanel extends Dialog {
    protected HLayout footerLayout = new HLayout(5);
	protected HLayout headerLayout = new HLayout();
	protected VLayout contextArea = new VLayout();
	
	final private static Map<Severity,String> CONTEXT_LAYOUT_FRAME_COLORS = new HashMap<Severity, String>() {{
		put(Severity.ERROR, "#FF3300");
		put(Severity.WARNING, "#FF9900");		
		put(Severity.INFO, "#CED8DF");
	}};
	
	public NonDraggableModalPanel(int width, int height, String title) {
		this(width, height, title, Severity.INFO, 5);
	}
	
	public NonDraggableModalPanel(int width, int height, String title, Severity severity) {
		this(width, height, title, Severity.INFO, 5);
	}
	
	public NonDraggableModalPanel(int width, int height, String title, int inPanelPadding) {
		this(width, height, title, Severity.INFO, inPanelPadding);
	}
	
	public NonDraggableModalPanel(int width, int height, String title, Severity severity, int inPanelPadding) {
		setToolbarButtons(footerLayout);
		
		setIsModal(true);
		setShowModalMask(true);  
		setWidth(width);
		setHeight(height);
		setShowMinimizeButton(false);
		setShowCloseButton(false);
		
		setAttribute("edgeTop", 14, false);
		setAttribute("edgeBottom", 14, false);
		setShowTitle(false);
		setShowHeader(false);
		setEdgeImage("dialogs/dialog_edge.png");
		setEdgeSize(14);
		setShowEdges(true);
		setFooterHeight(0);
		
		setEdgeOffset(4);
		setMargin(-2);

		
		setShowResizeBar(false);
		setShowResizer(false);
		setCanDrag(false);
		setCanDragResize(false);
		setCanDragReposition(false);	
		centerInPage();	
		setDismissOnEscape(true);
				
		headerLayout.setWidth100();
		headerLayout.setHeight(27);

		addCloseClickHandler(new CloseClickHandler() {
			@Override
			public void onCloseClick(CloseClientEvent event) {
				onClose();
			}
		});
		
		HLayout headerLeft = new HLayout();
		Img headerLeftImage = new Img("dialogs/" + severity.toString() + ".png", 27, 27);
		headerLeft.setWidth(27);
		headerLeft.addMember(headerLeftImage);
		
		HLayout headerRight= new HLayout();
		Img headerRightImage = new Img("dialogs/dialog_header_right.png", 118, 27);
		headerRight.setWidth(118);
		headerRight.addMember(headerRightImage);
		headerRight.setAlign(Alignment.RIGHT);

		HLayout headerCenter= new HLayout();
		headerCenter.setWidth100();
		headerCenter.setHeight(27);
		headerCenter.setBackgroundImage("dialogs/dialog_header_background.png");
		headerCenter.setBackgroundRepeat(BkgndRepeat.REPEAT_X);
		
		Label dialogTitle = new Label(title);
		dialogTitle.setStyleName("dialog-header-title");
		dialogTitle.setAutoFit(true);
		headerCenter.addMember(dialogTitle);
		
		headerLayout.setMembers(headerLeft, headerCenter, headerRight);
		headerLayout.setStyleName("dialog-header");
		super.addItem(headerLayout);

		contextArea.setHeight100();
		contextArea.setWidth100();
		contextArea.setBorder("solid 1px " + CONTEXT_LAYOUT_FRAME_COLORS.get(severity));
		contextArea.setMargin(2);
		contextArea.setPadding(inPanelPadding);
		super.addItem(contextArea);
		
	}

    @Override
    protected void onDraw() {
        // get body auto-child
        Canvas body = getById(getID() + "_body");
        // disable SmartClient custom scrollbars (#656384, #698988)
        // this is required because the custom scrollbars feature catches arrow key events also inside text fields.
        body.setShowCustomScrollbars(false);
    }

	@Override
    public void addItem(Canvas component) {
		contextArea.addMember(component);
	}
	
	public HLayout getFooterLayout() {
		return footerLayout;
	}
	
	public HLayout getHeaderLayout() {
        return headerLayout;
    }

    public VLayout getContextArea() {
        return contextArea;
    }
	
	public void setFooterButtons(Alignment alignment, Canvas... buttons) {
		footerLayout.setAlign(alignment);
		footerLayout.setMembers(buttons);
	}	

    public void onClose() {
	}
}