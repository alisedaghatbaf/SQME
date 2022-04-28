package hcsanplugin.model;

public class Token {
	String type, value;

	public Token(String t, String v) {
		type = t;
		value = v;
	}

	public String toString() {
		return "(" + type + ", " + value + ")";
	}
}
