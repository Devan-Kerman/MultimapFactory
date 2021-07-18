package multimapfactory.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface IntIterator extends Iterator<Integer> {
	interface Able extends Iterable<Integer> {
		@Override
		IntIterator iterator();
	}

	int nextInt();

	@Override
	default Integer next() {
		return this.nextInt();
	}

	default void forEachRemaining(IntConsumer consumer) {
		while(this.hasNext()) {
			consumer.accept(this.nextInt());
		}
	}

	@Override
	default void forEachRemaining(Consumer<? super Integer> consumer) {
		if(consumer instanceof IntConsumer) {
			this.forEachRemaining((IntConsumer) consumer);
		} else {
			while(this.hasNext()) {
				consumer.accept(this.next());
			}
		}
	}

	default long skip(long amount) {
		long i;
		for(i = 0; i < amount && this.hasNext(); i++) {
			this.nextInt();
		}
		return i;
	}
}
