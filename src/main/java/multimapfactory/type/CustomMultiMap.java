package multimapfactory.type;

import multimapfactory.util.Exceptions;

public interface CustomMultiMap<Self extends CustomMultiMap<Self>> {
	default boolean isEmpty() {
		return this.size() == 0;
	}

	default int size() {
		throw Exceptions.impl("size");
	}

	default void putAll(Self self) {
		throw Exceptions.impl("putAll");
	}

	default void clear() {
		throw Exceptions.impl("clear");
	}
}
