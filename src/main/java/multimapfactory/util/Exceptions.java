package multimapfactory.util;

public class Exceptions {
	public static UnsupportedOperationException value(String value) {
		return new UnsupportedOperationException("multimap factory did not recognize value '"+ value + "'!");
	}

	public static UnsupportedOperationException impl(String value) {
		return new UnsupportedOperationException("multimap factory did not recognize impl '"+ value + "'!");
	}
}
