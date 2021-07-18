package multimapfactory.type;

/**
 * Represents a map that may have multiple keys, but only one value.
 * It must have 2 methods, one for getting, and one for putting, both must be abstract.
 * The getter should return your value type, and the setter should have it as an additional parameter
 */
public interface SingleValueMap<Self extends SingleValueMap<Self>> extends CustomMultiMap<Self> {}
