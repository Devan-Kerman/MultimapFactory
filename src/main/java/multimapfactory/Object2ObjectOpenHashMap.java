package multimapfactory;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;

import java.util.Arrays;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;

public class Object2ObjectOpenHashMap<K, V> extends AbstractObject2ObjectMap<K, V> implements java.io.Serializable, Cloneable, Hash {
	private static final long serialVersionUID = 0L;
	private static final boolean ASSERTS = false;
	protected final transient int minN;
	protected final float loadFactor;
	protected transient K[] key;
	protected transient V[] value;
	protected transient int mask;
	protected transient boolean containsNullKey;
	protected transient int n;
	protected transient int maxFill;
	protected int size;

	@SuppressWarnings("unchecked")
	public Object2ObjectOpenHashMap(final int expected, final float loadFactor) {
		if(loadFactor <= 0 || loadFactor >= 1) {
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
		}
		if(expected < 0) {
			throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		}
		this.loadFactor = loadFactor;
		minN = n = arraySize(expected, loadFactor);
		mask = n - 1;
		maxFill = maxFill(n, loadFactor);
		key = (K[]) new Object[n + 1];
		value = (V[]) new Object[n + 1];
	}

	private int find(final K k) {
		if(((k) == null)) {
			return containsNullKey ? n : -(n + 1);
		}
		K curr;
		final K[] key = this.key;
		int pos;

		if(((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask]) == null)) {
			return -(pos + 1);
		}
		if(((k).equals(curr))) {
			return pos;
		}

		while(true) {
			if(((curr = key[pos = (pos + 1) & mask]) == null)) {
				return -(pos + 1);
			}
			if(((k).equals(curr))) {
				return pos;
			}
		}
	}

	private void insert(final int pos, final K k, final V v) {
		if(pos == n) {
			containsNullKey = true;
		}
		key[pos] = k;
		value[pos] = v;
		if(size++ >= maxFill) {
			rehash(arraySize(size + 1, loadFactor));
		}
		if(ASSERTS) {
			checkTable();
		}
	}

	@SuppressWarnings("unchecked")
	protected void rehash(final int newN) {
		final K key[] = this.key;
		final V value[] = this.value;
		final int mask = newN - 1;
		final K newKey[] = (K[]) new Object[newN + 1];
		final V newValue[] = (V[]) new Object[newN + 1];
		int i = n, pos;
		for(int j = realSize(); j-- != 0; ) {
			while(((key[--i]) == null)) {

			}
			if(!((newKey[pos = (it.unimi.dsi.fastutil.HashCommon.mix((key[i]).hashCode())) & mask]) == null)) {
				while(!((newKey[pos = (pos + 1) & mask]) == null)) {

				}
			}
			newKey[pos] = key[i];
			newValue[pos] = value[i];
		}
		newValue[newN] = value[n];
		n = newN;
		this.mask = mask;
		maxFill = maxFill(n, loadFactor);
		this.key = newKey;
		this.value = newValue;
	}

	private void checkTable() {}

	private int realSize() {
		return containsNullKey ? size - 1 : size;
	}

	private void ensureCapacity(final int capacity) {
		final int needed = arraySize(capacity, loadFactor);
		if(needed > n) {
			rehash(needed);
		}
	}

	private void tryCapacity(final long capacity) {
		final int needed = (int) Math.min(1 << 30, Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / loadFactor))));
		if(needed > n) {
			rehash(needed);
		}
	}

	private V removeEntry(final int pos) {
		final V oldValue = value[pos];
		value[pos] = null;
		size--;
		shiftKeys(pos);
		if(n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
			rehash(n / 2);
		}
		return oldValue;
	}

	private V removeNullEntry() {
		containsNullKey = false;
		key[n] = null;
		final V oldValue = value[n];
		value[n] = null;
		size--;
		if(n > minN && size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) {
			rehash(n / 2);
		}
		return oldValue;
	}

	protected final void shiftKeys(int pos) {
		int last, slot;
		K curr;
		final K[] key = this.key;
		for(; ; ) {
			pos = ((last = pos) + 1) & mask;
			for(; ; ) {
				if(((curr = key[pos]) == null)) {
					key[last] = (null);
					value[last] = null;
					return;
				}
				slot = (it.unimi.dsi.fastutil.HashCommon.mix((curr).hashCode())) & mask;
				if(last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) {
					break;
				}
				pos = (pos + 1) & mask;
			}
			key[last] = curr;
			value[last] = value[pos];
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(final Object k) {
		if((((K) k) == null)) {
			return containsNullKey ? value[n] : defRetValue;
		}
		K curr;
		final K[] key = this.key;
		int pos;

		if(((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask]) == null)) {
			return defRetValue;
		}
		if(((k).equals(curr))) {
			return value[pos];
		}

		while(true) {
			if(((curr = key[pos = (pos + 1) & mask]) == null)) {
				return defRetValue;
			}
			if(((k).equals(curr))) {
				return value[pos];
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean containsKey(final Object k) {
		if((((K) k) == null)) {
			return containsNullKey;
		}
		K curr;
		final K[] key = this.key;
		int pos;

		if(((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask]) == null)) {
			return false;
		}
		if(((k).equals(curr))) {
			return true;
		}

		while(true) {
			if(((curr = key[pos = (pos + 1) & mask]) == null)) {
				return false;
			}
			if(((k).equals(curr))) {
				return true;
			}
		}
	}

	@Override
	public boolean containsValue(final Object v) {
		final V value[] = this.value;
		final K key[] = this.key;
		if(containsNullKey && java.util.Objects.equals(value[n], v)) {
			return true;
		}
		for(int i = n; i-- != 0; ) {
			if(!((key[i]) == null) && java.util.Objects.equals(value[i], v)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		if(size == 0) {
			return;
		}
		size = 0;
		containsNullKey = false;
		Arrays.fill(key, (null));
		Arrays.fill(value, null);
	}

	@Override
	public V put(final K k, final V v) {
		final int pos = find(k);
		if(pos < 0) {
			insert(-pos - 1, k, v);
			return defRetValue;
		}
		final V oldValue = value[pos];
		value[pos] = v;
		return oldValue;
	}

	@Override
	public V remove(final Object k) {
		if((((K) k) == null)) {
			if(containsNullKey) {
				return removeNullEntry();
			}
			return defRetValue;
		}
		K curr;
		final K[] key = this.key;
		int pos;

		if(((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask]) == null)) {
			return defRetValue;
		}
		if(((k).equals(curr))) {
			return removeEntry(pos);
		}
		while(true) {
			if(((curr = key[pos = (pos + 1) & mask]) == null)) {
				return defRetValue;
			}
			if(((k).equals(curr))) {
				return removeEntry(pos);
			}
		}
	}
}
