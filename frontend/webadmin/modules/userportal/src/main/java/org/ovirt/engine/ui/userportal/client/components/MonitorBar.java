package org.ovirt.engine.ui.userportal.client.components;

import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.layout.VLayout;

public class MonitorBar extends VLayout {
	
	HStack bar = new HStack();
	Label percentageTitle = new Label("0%");
	int currPercentage;
	int maxWidth;

	public MonitorBar(String title, String color, int width, int height) {
		this(title, color, width, height, "monitorBarTitle");
	}
	
	public MonitorBar(String title, String color, int width, int height, String titleStyle) {
		setAutoWidth();
		setAutoHeight();
		maxWidth = width;
		Label titleLabel = new Label(title + ":");
		titleLabel.setAutoHeight();
		titleLabel.setAutoWidth();
		titleLabel.setWrap(false);
		titleLabel.setStyleName(titleStyle);
		
		bar.setBackgroundColor(color);
		bar.setHeight(height);
		bar.hide();
		HStack border = new HStack();
		border.setHeight(height + 2);
		border.setWidth(maxWidth + 2);
		border.setBorder("1px solid #C9D6E6");
		border.setPadding(1);
		border.addMember(bar);

		HStack box = new HStack();

		percentageTitle.setAutoHeight();
		percentageTitle.setAutoWidth();
		percentageTitle.setStyleName("monitorBarPercentage");

		box.addMember(border);
		box.addMember(percentageTitle);
		box.setMembersMargin(4);

		setMembersMargin(2);
		addMember(titleLabel);
		addMember(box);
	}

	public void setBarPercentage(int percentage) {
		if (currPercentage != percentage) {
			if (currPercentage == 0)
				bar.show();
			if (percentage == 0) {
				bar.hide();
			}
			String width = percentage + "%";
			bar.setWidth(width);
			percentageTitle.setContents(width);
			currPercentage = percentage;
		}
	}
}
