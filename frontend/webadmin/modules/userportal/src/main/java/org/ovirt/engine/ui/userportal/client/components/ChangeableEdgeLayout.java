package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.types.BkgndRepeat;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;

public abstract class ChangeableEdgeLayout extends Canvas {
	Canvas topLeft;
	Canvas top;
	Canvas topRight;
	Canvas left;
	Canvas right;
	Canvas bottomLeft;
	Canvas bottom;
	Canvas bottomRight;
	Layout layout;

	public ChangeableEdgeLayout(int width, int height, String edgeImagePrefix, String edgeImageSuffix, int edgeSize, String id) {
		super.setHeight(height + edgeSize*2);
		super.setWidth(width + edgeSize*2);
		super.setOverflow(Overflow.HIDDEN);

		topLeft = new Canvas();
		topLeft.setHeight(edgeSize);
		topLeft.setWidth(edgeSize);
		topLeft.setBackgroundRepeat(BkgndRepeat.NO_REPEAT);
		super.addChild(topLeft);

		top = new Canvas();
		top.setHeight(edgeSize);
		top.setWidth(width);
		top.setBackgroundRepeat(BkgndRepeat.REPEAT_X);
		top.setLeft(edgeSize);
		top.setWidth(width);
		super.addChild(top);

		topRight = new Canvas();
		topRight.setHeight(edgeSize);
		topRight.setWidth(edgeSize);
		topRight.setBackgroundRepeat(BkgndRepeat.NO_REPEAT);
		topRight.setLeft(width + edgeSize);
		super.addChild(topRight);

		left = new Canvas();
		left.setWidth(edgeSize);
		left.setHeight(height);
		left.setBackgroundRepeat(BkgndRepeat.REPEAT_Y);
		left.setTop(edgeSize);
		left.setHeight(height);
		super.addChild(left);

		right = new Canvas();
		right.setWidth(edgeSize);
		right.setHeight(height);
		right.setBackgroundRepeat(BkgndRepeat.REPEAT_Y);
		right.setTop(edgeSize);
		right.setLeft(width + edgeSize);
		right.setHeight(height);
		super.addChild(right);

		bottomLeft = new Canvas();
		bottomLeft.setWidth(edgeSize);
		bottomLeft.setHeight(edgeSize);
		bottomLeft.setBackgroundRepeat(BkgndRepeat.NO_REPEAT);
		bottomLeft.setTop(height + edgeSize);
		super.addChild(bottomLeft);

		bottom = new Canvas();
		bottom.setWidth(width);
		bottom.setHeight(edgeSize);
		bottom.setBackgroundRepeat(BkgndRepeat.REPEAT_X);
		bottom.setTop(height + edgeSize);
		bottom.setLeft(edgeSize);
		bottom.setWidth(width);
		super.addChild(bottom);

		bottomRight = new Canvas();
		bottomRight.setWidth(edgeSize);
		bottomRight.setHeight(edgeSize);
		bottomRight.setBackgroundRepeat(BkgndRepeat.NO_REPEAT);
		bottomRight.setTop(height + edgeSize);
		bottomRight.setLeft(width + edgeSize);
		super.addChild(bottomRight);

		setEdgeImage(edgeImagePrefix, edgeImageSuffix);

		layout = getLayout();
		layout.setWidth(width);
		layout.setHeight(height);
		layout.setTop(edgeSize);
		layout.setLeft(edgeSize);
		super.addChild(layout);

	}	

	public void setEdgeImage(String edgeImagePrefix, String edgeImageSuffix) {
		topLeft.setBackgroundImage(edgeImagePrefix + "_TL." + edgeImageSuffix);
		top.setBackgroundImage(edgeImagePrefix + "_T." + edgeImageSuffix);
		topRight.setBackgroundImage(edgeImagePrefix + "_TR." + edgeImageSuffix);
		left.setBackgroundImage(edgeImagePrefix + "_L." + edgeImageSuffix);
		right.setBackgroundImage(edgeImagePrefix + "_R." + edgeImageSuffix);
		bottomLeft.setBackgroundImage(edgeImagePrefix + "_BL." + edgeImageSuffix);
		bottom.setBackgroundImage(edgeImagePrefix + "_B." + edgeImageSuffix);
		bottomRight.setBackgroundImage(edgeImagePrefix + "_BR." + edgeImageSuffix);
	}
	
	protected abstract Layout getLayout();

    public void setMembersMargin(int membersMargin) {
    	layout.setMembersMargin(membersMargin);
    }
    
    public void addMember(Canvas component) {
    	layout.addMember(component);
    }
    
    public void setHeight(int height) {
        layout.setHeight(height);
    }

    public void setHeight100() {
        layout.setHeight100();
    }

    public void setWidth100() {
        layout.setWidth100();
    }

    public void setHeight(String height) {
        layout.setHeight(height);
    }
}
