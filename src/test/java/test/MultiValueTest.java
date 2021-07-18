package test;

import multimapfactory.util.Exceptions;
import multimapfactory.type.IndexMultiValueMap;
import multimapfactory.an.Value;

public interface MultiValueTest extends IndexMultiValueMap {
	int get(Object key1, long key2, long key3);

	@Value
	default long value1(int index) {throw Exceptions.value("value1");}

	@Value
	default char value2(int index) {throw Exceptions.value("value2");}
}
