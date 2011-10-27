package org.ovirt.engine.ui.uicompat;

import java.util.Iterator;

public class IteratorUtils {

	/// <summary>

	/// Advances to the next item in the specified enumerator and returns true.

	/// If no item has left to advance to - return false

	/// </summary>

	/// <param name="enumerator">specified enumerator</param>

	/// <returns>true if advanced to next item, false otherwise</returns>

	@SuppressWarnings("rawtypes")
	public static boolean moveNext(Iterator iterator) {
		if (iterator.hasNext()) {
			iterator.next();
			return true;
		}
		else {
			return false;
		}
	}
}
