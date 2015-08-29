package rx.observables;

import rx.Single;
import rx.SingleSubscriber;
import rx.annotations.Experimental;
import rx.internal.operators.BlockingOperatorToFuture;
import java.util.concurrent.Future;

/**
 * {@code BlockingSingle} is a blocking "version" of {@link Single} that provides blocking
 * operators.
 *
 * You construct a {@code BlockingSingle} from a {@code Single} with {@link #from(Single)}
 * or {@link Single#toBlocking()}.
 */
@Experimental
public class BlockingSingle<T> {
	private final Single<? extends T> single;

	private BlockingSingle(Single<? extends T> single) {
		this.single = single;
	}
	
	/**
	 * Converts a {@link Single} into a {@code BlockingSingle}.
	 *
	 * @param single the {@link Single} you want to convert

	 * @return a {@code BlockingSingle} version of {@code single}
	 */
	public static <T> BlockingSingle<T> from(Single<? extends T> single) {
		return new BlockingSingle<T>(single);
	}

	/**
	 * Returns the item emitted by this {@code BlockingSingle}.
	 *
	 * If the underlying {@link Single} returns successfully, the value emitted
	 * by the {@link Single} is returned. If the {@link Single} emits an error,
	 * the throwable emitted ({@link SingleSubscriber#onError(Throwable)}) is
	 * thrown.
	 *
	 *  @return the value emitted by this {@code BlockingSingle}
	 */
	public T get() {
		return single.toObservable().toBlocking().single();
	}

	/**
	 * Returns a {@link Future} representing the value emitted by this {@code BlockingSingle}.
	 *
	 * @return a {@link Future} that returns the value
	 */
	public Future<T> toFuture() {
		return BlockingOperatorToFuture.toFuture(single.toObservable());
	}
}

