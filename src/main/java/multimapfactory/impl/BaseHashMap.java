package multimapfactory.impl;

import multimapfactory.type.CustomHashMultiMap;

@Template
public class BaseHashMap implements CustomHashMultiMap<BaseHashMap> {
	Object nullValue;

	Object[] keys;
	Object[] values;

	@Template
	private static int find(final Object k, Object[] keys, int from, int mask, int hashCode) {
		if(((k) == null)) {
			return containsNullKey ? n : -(n + 1);
		}
		Object curr;
		int pos;

		if(((curr = keys[pos = (it.unimi.dsi.fastutil.HashCommon.mix(hashCode)) & mask]) == null)) {
			return -(pos + 1);
		}
		if(((k).equals(curr))) {
			return pos;
		}

		while(true) {
			if(((curr = keys[pos = (pos + 1) & mask]) == null)) {
				return -(pos + 1);
			}
			if(((k).equals(curr))) {
				return pos;
			}
		}
	}

	@Override
	public boolean trim() {
		return CustomHashMultiMap.super.trim();
	}

	@Override
	public boolean trim(int size) {
		return CustomHashMultiMap.super.trim(size);
	}

	@Override
	public boolean isEmpty() {
		return CustomHashMultiMap.super.isEmpty();
	}

	@Override
	public int size() {
		return CustomHashMultiMap.super.size();
	}

	@Override
	public void putAll(BaseHashMap map) {
		CustomHashMultiMap.super.putAll(map);
	}

	@Override
	public void clear() {
		CustomHashMultiMap.super.clear();
	}
}
