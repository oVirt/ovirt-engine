package org.ovirt.engine.ui.uicompat;

import java.util.HashMap;

public class Translator extends HashMap<Object, String> {

	@Override
	public String get(Object key) {
		return key.toString();
	}

	public void add(Object key, String value) {
		put(key, value);
	}
}
