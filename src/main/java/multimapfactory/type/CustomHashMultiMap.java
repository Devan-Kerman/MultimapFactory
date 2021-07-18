package multimapfactory.type;

import multimapfactory.util.Exceptions;

public interface CustomHashMultiMap<Self extends CustomHashMultiMap<Self>> extends CustomMultiMap<Self> {
	default boolean trim() {
		return this.trim(this.size());
	}

	default boolean trim(int size) {
		throw Exceptions.impl("trim");
	}
}
