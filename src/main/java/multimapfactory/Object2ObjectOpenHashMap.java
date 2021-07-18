package multimapfactory;

import static it.unimi.dsi.fastutil.HashCommon.arraySize;
import static it.unimi.dsi.fastutil.HashCommon.maxFill;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;

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
	protected transient FastEntrySet<K, V> entries;

	protected transient ObjectSet<K> keys;

	protected transient ObjectCollection<V> values;

	public Object2ObjectOpenHashMap(final int expected) {
		this(expected, DEFAULT_LOAD_FACTOR);
	}

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

	public Object2ObjectOpenHashMap() {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	public Object2ObjectOpenHashMap(final Map<? extends K, ? extends V> m, final float loadFactor) {
		this(m.size(), loadFactor);
		putAll(m);
	}

	public Object2ObjectOpenHashMap(final Map<? extends K, ? extends V> m) {
		this(m, DEFAULT_LOAD_FACTOR);
	}

	public Object2ObjectOpenHashMap(final Object2ObjectMap<K, V> m, final float loadFactor) {
		this(m.size(), loadFactor);
		putAll(m);
	}

	public Object2ObjectOpenHashMap(final Object2ObjectMap<K, V> m) {
		this(m, DEFAULT_LOAD_FACTOR);
	}

	public Object2ObjectOpenHashMap(final K[] k, final V[] v) {
		this(k, v, DEFAULT_LOAD_FACTOR);
	}

	public Object2ObjectOpenHashMap(final K[] k, final V[] v, final float loadFactor) {
		this(k.length, loadFactor);
		if(k.length != v.length) {
			throw new IllegalArgumentException("The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
		}
		for(int i = 0; i < k.length; i++) {
			this.put(k[i], v[i]);
		}
	}

	@SuppressWarnings("unchecked")
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
	public ObjectSet<K> keySet() {
		if(keys == null) {
			keys = new KeySet();
		}
		return keys;
	}

	@Override
	public ObjectCollection<V> values() {
		if(values == null) {
			values = new AbstractObjectCollection<V>() {
				@Override
				public ObjectIterator<V> iterator() { return new ValueIterator(); }

				@Override
				public ObjectSpliterator<V> spliterator() { return new ValueSpliterator(); }

				@Override
				public void forEach(final Consumer<? super V> consumer) {
					if(containsNullKey) {
						consumer.accept(value[n]);
					}
					for(int pos = n; pos-- != 0; ) {
						if(!((key[pos]) == null)) {
							consumer.accept(value[pos]);
						}
					}
				}

				@Override
				public int size() { return size; }

				@Override
				public boolean contains(Object v) { return containsValue(v); }

				@Override
				public void clear() { Object2ObjectOpenHashMap.this.clear(); }
			};
		}
		return values;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		if(loadFactor <= .5) {
			ensureCapacity(m.size());
		} else {
			tryCapacity(size() + m.size());
		}
		super.putAll(m);
	}

	@Override
	public int hashCode() {
		int h = 0;
		for(int j = realSize(), i = 0, t = 0; j-- != 0; ) {
			while(((key[i]) == null)) {
				i++;
			}
			if(this != key[i]) {
				t = ((key[i]).hashCode());
			}
			if(this != value[i]) {
				t ^= ((value[i]) == null ? 0 : (value[i]).hashCode());
			}
			h += t;
			i++;
		}

		if(containsNullKey) {
			h += ((value[n]) == null ? 0 : (value[n]).hashCode());
		}
		return h;
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
	public FastEntrySet<K, V> object2ObjectEntrySet() {
		if(entries == null) {
			entries = new MapEntrySet();
		}
		return entries;
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
	@SuppressWarnings("unchecked")
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

	public boolean trim() {
		return trim(size);
	}

	public boolean trim(final int n) {
		final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / loadFactor));
		if(l >= this.n || size > maxFill(l, loadFactor)) {
			return true;
		}
		try {
			rehash(l);
		} catch(OutOfMemoryError cantDoIt) {
			return false;
		}
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object2ObjectOpenHashMap<K, V> clone() {
		Object2ObjectOpenHashMap<K, V> c;
		try {
			c = (Object2ObjectOpenHashMap<K, V>) super.clone();
		} catch(CloneNotSupportedException cantHappen) {
			throw new InternalError();
		}
		c.keys = null;
		c.values = null;
		c.entries = null;
		c.containsNullKey = containsNullKey;
		c.key = key.clone();
		c.value = value.clone();
		return c;
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		final K key[] = this.key;
		final V value[] = this.value;
		final EntryIterator i = new EntryIterator();
		s.defaultWriteObject();
		for(int j = size, e; j-- != 0; ) {
			e = i.nextEntry();
			s.writeObject(key[e]);
			s.writeObject(value[e]);
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		n = arraySize(size, loadFactor);
		maxFill = maxFill(n, loadFactor);
		mask = n - 1;
		final K key[] = this.key = (K[]) new Object[n + 1];
		final V value[] = this.value = (V[]) new Object[n + 1];
		K k;
		V v;
		for(int i = size, pos; i-- != 0; ) {
			k = (K) s.readObject();
			v = (V) s.readObject();
			if(((k) == null)) {
				pos = n;
				containsNullKey = true;
			} else {
				pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask;
				while(!((key[pos]) == null)) {
					pos = (pos + 1) & mask;
				}
			}
			key[pos] = k;
			value[pos] = v;
		}
		if(ASSERTS) {
			checkTable();
		}
	}

	final class MapEntry implements Object2ObjectMap.Entry<K, V>, Map.Entry<K, V>, it.unimi.dsi.fastutil.Pair<K, V> {

		int index;

		MapEntry(final int index) {
			this.index = index;
		}

		MapEntry() {}

		@Override
		public K getKey() {
			return key[index];
		}

		@Override
		public V getValue() {
			return value[index];
		}

		@Override
		public V setValue(final V v) {
			final V oldValue = value[index];
			value[index] = v;
			return oldValue;
		}

		@Override
		public K left() {
			return key[index];
		}

		@Override
		public V right() {
			return value[index];
		}

		@Override
		public it.unimi.dsi.fastutil.Pair<K, V> right(final V v) {
			value[index] = v;
			return this;
		}

		@Override
		public int hashCode() {
			return ((key[index]) == null ? 0 : (key[index]).hashCode()) ^ ((value[index]) == null ? 0 : (value[index]).hashCode());
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(final Object o) {
			if(!(o instanceof Map.Entry)) {
				return false;
			}
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			return java.util.Objects.equals(key[index], (e.getKey())) && java.util.Objects.equals(value[index], (e.getValue()));
		}

		@Override
		public String toString() {
			return key[index] + "=>" + value[index];
		}
	}

	private abstract class MapIterator<ConsumerType> {

		int pos = n;

		int last = -1;

		int c = size;

		boolean mustReturnNullKey = Object2ObjectOpenHashMap.this.containsNullKey;

		ObjectArrayList<K> wrapped;

		public void forEachRemaining(final ConsumerType action) {
			if(mustReturnNullKey) {
				mustReturnNullKey = false;
				acceptOnIndex(action, last = n);
				c--;
			}
			final K key[] = Object2ObjectOpenHashMap.this.key;
			while(c != 0) {
				if(--pos < 0) {

					last = Integer.MIN_VALUE;
					final K k = wrapped.get(-pos - 1);
					int p = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask;
					while(!((k).equals(key[p]))) {
						p = (p + 1) & mask;
					}
					acceptOnIndex(action, p);
					c--;
				} else if(!((key[pos]) == null)) {
					acceptOnIndex(action, last = pos);
					c--;
				}
			}
		}

		@SuppressWarnings("unused")
		abstract void acceptOnIndex(final ConsumerType action, final int index);

		public void remove() {
			if(last == -1) {
				throw new IllegalStateException();
			}
			if(last == n) {
				containsNullKey = false;
				key[n] = null;
				value[n] = null;
			} else if(pos >= 0) {
				shiftKeys(last);
			} else {

				Object2ObjectOpenHashMap.this.remove(wrapped.set(-pos - 1, null));
				last = -1;
				return;
			}
			size--;
			last = -1;
			if(ASSERTS) {
				checkTable();
			}
		}

		private void shiftKeys(int pos) {

			int last, slot;
			K curr;
			final K[] key = Object2ObjectOpenHashMap.this.key;
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
				if(pos < last) {
					if(wrapped == null) {
						wrapped = new ObjectArrayList<>(2);
					}
					wrapped.add(key[pos]);
				}
				key[last] = curr;
				value[last] = value[pos];
			}
		}

		public int skip(final int n) {
			int i = n;
			while(i-- != 0 && hasNext()) {
				nextEntry();
			}
			return n - i - 1;
		}

		public boolean hasNext() {
			return c != 0;
		}

		public int nextEntry() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			c--;
			if(mustReturnNullKey) {
				mustReturnNullKey = false;
				return last = n;
			}
			final K key[] = Object2ObjectOpenHashMap.this.key;
			for(; ; ) {
				if(--pos < 0) {

					last = Integer.MIN_VALUE;
					final K k = wrapped.get(-pos - 1);
					int p = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask;
					while(!((k).equals(key[p]))) {
						p = (p + 1) & mask;
					}
					return p;
				}
				if(!((key[pos]) == null)) {
					return last = pos;
				}
			}
		}
	}

	private final class EntryIterator extends MapIterator<Consumer<? super Object2ObjectMap.Entry<K, V>>>
			implements ObjectIterator<Object2ObjectMap.Entry<K, V>> {
		private MapEntry entry;

		@Override
		public MapEntry next() {
			return entry = new MapEntry(nextEntry());
		}

		@Override
		final void acceptOnIndex(final Consumer<? super Object2ObjectMap.Entry<K, V>> action, final int index) {
			action.accept(entry = new MapEntry(index));
		}

		@Override
		public void remove() {
			super.remove();
			entry.index = -1;
		}
	}

	private final class FastEntryIterator extends MapIterator<Consumer<? super Object2ObjectMap.Entry<K, V>>>
			implements ObjectIterator<Object2ObjectMap.Entry<K, V>> {
		private final MapEntry entry = new MapEntry();

		@Override
		public MapEntry next() {
			entry.index = nextEntry();
			return entry;
		}

		@Override
		final void acceptOnIndex(final Consumer<? super Object2ObjectMap.Entry<K, V>> action, final int index) {
			entry.index = index;
			action.accept(entry);
		}
	}

	private abstract class MapSpliterator<ConsumerType, SplitType extends MapSpliterator<ConsumerType, SplitType>> {

		int pos = 0;

		int max = n;

		int c = 0;

		boolean mustReturnNull = Object2ObjectOpenHashMap.this.containsNullKey;
		boolean hasSplit = false;

		MapSpliterator() {}

		MapSpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
			this.pos = pos;
			this.max = max;
			this.mustReturnNull = mustReturnNull;
			this.hasSplit = hasSplit;
		}

		public boolean tryAdvance(final ConsumerType action) {
			if(mustReturnNull) {
				mustReturnNull = false;
				++c;
				acceptOnIndex(action, n);
				return true;
			}
			final K key[] = Object2ObjectOpenHashMap.this.key;
			while(pos < max) {
				if(!((key[pos]) == null)) {
					++c;
					acceptOnIndex(action, pos++);
					return true;
				}
				++pos;
			}
			return false;
		}

		abstract void acceptOnIndex(final ConsumerType action, final int index);

		public void forEachRemaining(final ConsumerType action) {
			if(mustReturnNull) {
				mustReturnNull = false;
				++c;
				acceptOnIndex(action, n);
			}
			final K key[] = Object2ObjectOpenHashMap.this.key;
			while(pos < max) {
				if(!((key[pos]) == null)) {
					acceptOnIndex(action, pos);
					++c;
				}
				++pos;
			}
		}

		public long estimateSize() {
			if(!hasSplit) {

				return size - c;
			} else {


				return Math.min(size - c, (long) (((double) realSize() / n) * (max - pos)) + (mustReturnNull ? 1 : 0));
			}
		}

		public SplitType trySplit() {
			if(pos >= max - 1) {
				return null;
			}
			int retLen = (max - pos) >> 1;
			if(retLen <= 1) {
				return null;
			}
			int myNewPos = pos + retLen;
			int retPos = pos;
			int retMax = myNewPos;


			SplitType split = makeForSplit(retPos, retMax, mustReturnNull);
			this.pos = myNewPos;
			this.mustReturnNull = false;
			this.hasSplit = true;
			return split;
		}

		abstract SplitType makeForSplit(int pos, int max, boolean mustReturnNull);

		public long skip(long n) {
			if(n < 0) {
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			}
			if(n == 0) {
				return 0;
			}
			long skipped = 0;
			if(mustReturnNull) {
				mustReturnNull = false;
				++skipped;
				--n;
			}
			final K key[] = Object2ObjectOpenHashMap.this.key;
			while(pos < max && n > 0) {
				if(!((key[pos++]) == null)) {
					++skipped;
					--n;
				}
			}
			return skipped;
		}
	}

	private final class EntrySpliterator extends MapSpliterator<Consumer<? super Object2ObjectMap.Entry<K, V>>, EntrySpliterator>
			implements ObjectSpliterator<Object2ObjectMap.Entry<K, V>> {
		private static final int POST_SPLIT_CHARACTERISTICS = ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

		EntrySpliterator() {}

		EntrySpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
			super(pos, max, mustReturnNull, hasSplit);
		}

		@Override
		public int characteristics() {
			return hasSplit ? POST_SPLIT_CHARACTERISTICS : ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS;
		}

		@Override
		final void acceptOnIndex(final Consumer<? super Object2ObjectMap.Entry<K, V>> action, final int index) {
			action.accept(new MapEntry(index));
		}

		@Override
		final EntrySpliterator makeForSplit(int pos, int max, boolean mustReturnNull) {
			return new EntrySpliterator(pos, max, mustReturnNull, true);
		}
	}

	private final class MapEntrySet extends AbstractObjectSet<Entry<K, V>> implements FastEntrySet<K, V> {
		@Override
		public ObjectIterator<Object2ObjectMap.Entry<K, V>> iterator() { return new EntryIterator(); }

		@Override
		public ObjectIterator<Object2ObjectMap.Entry<K, V>> fastIterator() { return new FastEntryIterator(); }

		@Override
		public void fastForEach(final Consumer<? super Object2ObjectMap.Entry<K, V>> consumer) {
			final AbstractObject2ObjectMap.BasicEntry<K, V> entry = new AbstractObject2ObjectMap.BasicEntry<>();
			if(containsNullKey) {
				entry.key = key[n];
				entry.value = value[n];
				consumer.accept(entry);
			}
			for(int pos = n; pos-- != 0; ) {
				if(!((key[pos]) == null)) {
					entry.key = key[pos];
					entry.value = value[pos];
					consumer.accept(entry);
				}
			}
		}

		@Override
		public ObjectSpliterator<Object2ObjectMap.Entry<K, V>> spliterator() { return new EntrySpliterator(); }

		@Override
		public int size() {
			return size;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean contains(final Object o) {
			if(!(o instanceof Map.Entry)) {
				return false;
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			final K k = ((K) e.getKey());
			final V v = ((V) e.getValue());
			if(((k) == null)) {
				return Object2ObjectOpenHashMap.this.containsNullKey && java.util.Objects.equals(value[n], v);
			}
			K curr;
			final K[] key = Object2ObjectOpenHashMap.this.key;
			int pos;

			if(((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask]) == null)) {
				return false;
			}
			if(((k).equals(curr))) {
				return java.util.Objects.equals(value[pos], v);
			}

			while(true) {
				if(((curr = key[pos = (pos + 1) & mask]) == null)) {
					return false;
				}
				if(((k).equals(curr))) {
					return java.util.Objects.equals(value[pos], v);
				}
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(final Object o) {
			if(!(o instanceof Map.Entry)) {
				return false;
			}
			final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			final K k = ((K) e.getKey());
			final V v = ((V) e.getValue());
			if(((k) == null)) {
				if(containsNullKey && java.util.Objects.equals(value[n], v)) {
					removeNullEntry();
					return true;
				}
				return false;
			}
			K curr;
			final K[] key = Object2ObjectOpenHashMap.this.key;
			int pos;

			if(((curr = key[pos = (it.unimi.dsi.fastutil.HashCommon.mix((k).hashCode())) & mask]) == null)) {
				return false;
			}
			if(((curr).equals(k))) {
				if(java.util.Objects.equals(value[pos], v)) {
					removeEntry(pos);
					return true;
				}
				return false;
			}
			while(true) {
				if(((curr = key[pos = (pos + 1) & mask]) == null)) {
					return false;
				}
				if(((curr).equals(k))) {
					if(java.util.Objects.equals(value[pos], v)) {
						removeEntry(pos);
						return true;
					}
				}
			}
		}

		@Override
		public void clear() {
			Object2ObjectOpenHashMap.this.clear();
		}

		@Override
		public void forEach(final Consumer<? super Object2ObjectMap.Entry<K, V>> consumer) {
			if(containsNullKey) {
				consumer.accept(new AbstractObject2ObjectMap.BasicEntry<K, V>(key[n], value[n]));
			}
			for(int pos = n; pos-- != 0; ) {
				if(!((key[pos]) == null)) {
					consumer.accept(new AbstractObject2ObjectMap.BasicEntry<K, V>(key[pos], value[pos]));
				}
			}
		}
	}

	private final class KeyIterator extends MapIterator<Consumer<? super K>> implements ObjectIterator<K> {
		public KeyIterator() { super(); }


		@Override
		final void acceptOnIndex(final Consumer<? super K> action, final int index) {
			action.accept(key[index]);
		}

		@Override
		public K next() { return key[nextEntry()]; }
	}

	private final class KeySpliterator extends MapSpliterator<Consumer<? super K>, KeySpliterator> implements ObjectSpliterator<K> {
		private static final int POST_SPLIT_CHARACTERISTICS = ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

		KeySpliterator() {}

		KeySpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
			super(pos, max, mustReturnNull, hasSplit);
		}

		@Override
		public int characteristics() {
			return hasSplit ? POST_SPLIT_CHARACTERISTICS : ObjectSpliterators.SET_SPLITERATOR_CHARACTERISTICS;
		}

		@Override
		final void acceptOnIndex(final Consumer<? super K> action, final int index) {
			action.accept(key[index]);
		}

		@Override
		final KeySpliterator makeForSplit(int pos, int max, boolean mustReturnNull) {
			return new KeySpliterator(pos, max, mustReturnNull, true);
		}
	}

	private final class KeySet extends AbstractObjectSet<K> {
		@Override
		public ObjectIterator<K> iterator() { return new KeyIterator(); }

		@Override
		public ObjectSpliterator<K> spliterator() { return new KeySpliterator(); }

		@Override
		public void forEach(final Consumer<? super K> consumer) {
			if(containsNullKey) {
				consumer.accept(key[n]);
			}
			for(int pos = n; pos-- != 0; ) {
				final K k = key[pos];
				if(!((k) == null)) {
					consumer.accept(k);
				}
			}
		}

		@Override
		public int size() { return size; }

		@Override
		public boolean contains(Object k) { return containsKey(k); }

		@Override
		public boolean remove(Object k) {
			final int oldSize = size;
			Object2ObjectOpenHashMap.this.remove(k);
			return size != oldSize;
		}

		@Override
		public void clear() { Object2ObjectOpenHashMap.this.clear();}
	}

	private final class ValueIterator extends MapIterator<Consumer<? super V>> implements ObjectIterator<V> {
		public ValueIterator() { super(); }


		@Override
		final void acceptOnIndex(final Consumer<? super V> action, final int index) {
			action.accept(value[index]);
		}

		@Override
		public V next() { return value[nextEntry()]; }
	}

	private final class ValueSpliterator extends MapSpliterator<Consumer<? super V>, ValueSpliterator> implements ObjectSpliterator<V> {
		private static final int POST_SPLIT_CHARACTERISTICS =
				ObjectSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS & ~java.util.Spliterator.SIZED;

		ValueSpliterator() {}

		ValueSpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
			super(pos, max, mustReturnNull, hasSplit);
		}

		@Override
		public int characteristics() {
			return hasSplit ? POST_SPLIT_CHARACTERISTICS : ObjectSpliterators.COLLECTION_SPLITERATOR_CHARACTERISTICS;
		}

		@Override
		final void acceptOnIndex(final Consumer<? super V> action, final int index) {
			action.accept(value[index]);
		}

		@Override
		final ValueSpliterator makeForSplit(int pos, int max, boolean mustReturnNull) {
			return new ValueSpliterator(pos, max, mustReturnNull, true);
		}
	}
}
