package org.ovirt.engine.ui.userportal.client.common;

public enum UserPortalMode {
	BASIC("Basic"), EXTENDED("Extended");

	public String title;

	UserPortalMode(String title) {
		this.title = title;
	}
}
