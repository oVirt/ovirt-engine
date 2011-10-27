package org.ovirt.engine.ui.userportal.client.components;

import org.ovirt.engine.ui.userportal.client.components.NonDraggableModalPanel;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class ProgressPanel extends VLayout {

    public ProgressPanel(int width, int height) {
		super();		
		
		// Set style
        setWidth(width);
        setHeight(height);
			
		// Create background panel
		NonDraggableModalPanel backgroundPanel = new NonDraggableModalPanel(width, height, "", 5);
		backgroundPanel.getFooterLayout().setVisible(false);
		backgroundPanel.getHeaderLayout().setVisible(false);
		backgroundPanel.getContextArea().setVisible(false);
		backgroundPanel.setShowToolbar(false);
		backgroundPanel.setOpacity(60);
		
		// Create animation panel
		HLayout animationPanel = new HLayout();
		animationPanel.setWidth100();
		animationPanel.setHeight100();
		animationPanel.setAlign(Alignment.CENTER);
		animationPanel.setDefaultLayoutAlign(VerticalAlignment.CENTER);
		
		// Add progress animation image
		Img progressAnimation = new Img("progress_spinner.gif", 32, 32);
		animationPanel.addMember(progressAnimation);		
		
		// Add panels
		addMember(backgroundPanel);
		addChild(animationPanel);
    }
}