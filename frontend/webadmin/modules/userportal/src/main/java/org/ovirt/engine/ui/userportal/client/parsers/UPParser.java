package org.ovirt.engine.ui.userportal.client.parsers;

public abstract class UPParser {

	abstract public String parse(String s);

	public String format(String s) {
		return s;
	}
}