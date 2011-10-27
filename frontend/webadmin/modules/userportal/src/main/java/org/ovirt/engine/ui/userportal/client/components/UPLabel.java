package org.ovirt.engine.ui.userportal.client.components;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.smartgwt.client.widgets.Label;

/**
 * Basically the same as the plain SmartGWT label but it auto fits and set wrapping to false and gives the option to put the style name in the constructor for
 * convenience.
 * The SmartGWT label also has a problem that when the content is set as null it ignores it instead we set the content to "" when the param is null
 */

public class UPLabel extends Label {

	private String contents = "";

	public UPLabel() {
		setAutoHeight();
		setAutoWidth();
		setWrap(false);
	}

	public UPLabel(String styleName) {
		this();
		setStyleName(styleName);
	}

	public UPLabel(String contents, String styleName) {
		this();
		setStyleName(styleName);
		setContents(contents);
	}

	@Override
	public void setContents(String contents) {
		setContents(contents, false);
	}

	public void setContents(String contents, boolean showContentsTooltip) {
		if (contents == null) {
			if (!this.contents.equals("")) {
				super.setContents("&nbsp;");
				this.contents = "";
				if (showContentsTooltip) {
					setShowHover(false);
				}
			}
		}
		else {
			if (!this.contents.equals(contents)) {
				String parsedContents = SafeHtmlUtils.fromString(contents).asString(); 
				super.setContents(parsedContents);
				this.contents = contents;
				if (showContentsTooltip) {
					setShowHover(true);
					int width = (contents.length() * 7);
					if (width > 400)
						width = 400;
					setHoverWidth(width);
					setTooltip(parsedContents);
				}
			}
		}
	}

	public void setHtmlContents(String contents) {
		if (contents == null) {
			if (!this.contents.equals("")) {
				super.setContents("&nbsp;");
				this.contents = "";
			}
		}
		else {
			if (!this.contents.equals(contents)) {
				super.setContents(contents);
				this.contents = contents;
			}
		}
	}
	
	@Override
	public String getContents() {
		return contents;
	}
}
