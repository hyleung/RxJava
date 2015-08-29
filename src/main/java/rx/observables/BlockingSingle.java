package rx.observables;

import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.annotations.Experimental;
import rx.internal.operators.BlockingOperatorToFuture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

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
		final AtomicReference<T> returnItem = new AtomicReference<T>();
		final AtomicReference<Throwable> returnException = new AtomicReference<Throwable>();
		final CountDownLatch latch = new CountDownLatch(1);
		Subscription subscription = single.subscribe(new SingleSubscriber<T>() {
			@Override
			public void onSuccess(T value) {
				returnItem.set(value);
				latch.countDown();
			}

			@Override
			public void onError(Throwable error) {
				returnException.set(error);
				latch.countDown();
			}
		});
		awaitForComplete(latch, subscription);
		Throwable throwable = returnException.get();
		if (throwable != null) {
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException)throwable;
			}
			throw new RuntimeException(throwable);
		}
		return returnItem.get();
	}

	/**
	 * Returns a {@link Future} representing the value emitted by this {@code BlockingSingle}.
	 *
	 * @return a {@link Future} that returns the value
	 */
	public Future<T> toFuture() {
		return BlockingOperatorToFuture.toFuture(single.toObservable());
	}

	private void awaitForComplete(CountDownLatch latch, Subscription subscription) {
		if (latch.getCount() == 0) {
			// Synchronous observable completes before awaiting for it.
			// Skip await so InterruptedException will never be thrown.
			return;
		}
		// block until the subscription completes and then return
		try {
			latch.await();
		} catch (InterruptedException e) {
			subscription.unsubscribe();
			// set the interrupted flag again so callers can still get it
			// for more information see https://github.com/ReactiveX/RxJava/pull/147#issuecomment-13624780
			Thread.currentThread().interrupt();
			// using Runtime so it is not checked
			throw new RuntimeException("Interrupted while waiting for subscription to complete.", e);
		}
	}
}

