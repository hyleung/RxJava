package rx.observables;


import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Single;
import rx.Subscriber;
import rx.exceptions.TestException;

import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

/**
 * Test suite for {@link BlockingSingle}.
 */
public class BlockingSingleTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Mock
	Subscriber<Integer> w;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testSingleGet() {
		Single<String> single = Single.just("one");
		BlockingSingle<? extends String> blockingSingle = BlockingSingle.from(single);
		assertEquals("one", blockingSingle.get());
	}

	@Test
	public void testSingleError() {
		Single<String> single = Single.error(new TestException());
		BlockingSingle<? extends String> blockingSingle = BlockingSingle.from(single);
		exception.expect(TestException.class);
		blockingSingle.get();
	}
	@Test
	public void testSingleErrorChecked() {
		Single<String> single = Single.error(new TestCheckedException());
		BlockingSingle<? extends String> blockingSingle = BlockingSingle.from(single);
		exception.expect(throwableWithCause(TestCheckedException.class));
		blockingSingle.get();
	}

	@Test
	public void testSingleToFuture() throws Exception {
		Single<String> single = Single.just("one");
		BlockingSingle<? extends String> blockingSingle = BlockingSingle.from(single);
		Future<? extends String> future = blockingSingle.toFuture();
		String result = future.get();
		assertEquals("one", result);
	}

	static ThrowableWithCause throwableWithCause(Class<? extends Throwable> expected) {
		return new ThrowableWithCause(expected);
	}

	private static final class TestCheckedException extends Exception {
	}


	private static class ThrowableWithCause extends BaseMatcher<Throwable> {
		private final Class<? extends Throwable> expected;
		private Throwable actual;
		private ThrowableWithCause(Class<? extends Throwable> expected) {
			this.expected = expected;
		}

		@Override
		public boolean matches(Object o) {
			if (o instanceof RuntimeException) {
				actual = (RuntimeException) o;
				return actual.getCause().getClass().equals(expected);
			}
			return false;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(String
					.format("Expected: %s, Actual: %s",
							expected.getCanonicalName(),
							actual.getClass().getCanonicalName()));
		}
	}
}
