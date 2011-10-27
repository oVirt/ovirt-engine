package org.ovirt.engine.core.compat;

public class StringBuilderCompat {

	protected static String SEP = ("\n");

	protected StringBuilder sb = new StringBuilder();

	public StringBuilderCompat(String string) {
		sb.append(string);
	}

	public StringBuilderCompat() {
	}

	public int length() {
		return sb.length();
	}

	public void replace(String oldText, String newText) {
		String t = sb.toString();
		sb = new StringBuilder(t.replaceAll(StringHelper.quote(oldText), newText));
	}

	public void append(String text) {
		sb.append(text);
	}

	public void AppendLine(String text) {
		sb.append(text);
		sb.append(SEP);
	}

	public void append(boolean bool) {
		sb.append(bool);

	}

	public void append(Object obj) {
		sb.append(obj);
	}

	public void delete(int start, int length) {
		sb.delete(start, start + length);
	}

	public String toString() {
		return sb.toString();
	}

}
