package safeprintstacktrace;

import org.apache.commons.lang.exception.ExceptionUtils;

public class DetectCircularReferences {

	private static final String CIRCULAR_REFERENCE_MESSAGE_ON_DETECTION = "Avoided circular reference";
	private static final int DEFAULT_MAX_DEEP = 8;

	public static void main(String[] args) {
		Throwable t0 = new Throwable("0");
		Throwable t1 = new Throwable("1");
		Throwable t2 = new Throwable("2");
		t0.initCause(t1);
		t1.initCause(t2);
		t2.initCause(t1); // circular reference
		try {
			throw t0;
		} catch (Throwable th) {
			final String safeStacktrace = getSafeStackTrace(th, DEFAULT_MAX_DEEP);
			System.out.println(safeStacktrace);
		}
	}

	private static String getSafeStackTrace(Throwable th, final int maxDeep) {
		int currentDeep = 0;
		Throwable parent = th;
		while (parent != null && currentDeep < maxDeep) {
			Throwable circularReferenceCause = getCircularReferenceCause(parent, maxDeep - currentDeep);
			if (circularReferenceCause != null) {
				return getStackTraceWithoutCircularReference(th, circularReferenceCause);
			}
			parent = parent.getCause();
			currentDeep++;
		}
		return ExceptionUtils.getStackTrace(th);
	}

	private static Throwable getCircularReferenceCause(Throwable th, final int maxDeep) {
		int currentDeep = 0;
		Throwable deepCause = th.getCause();
		while (deepCause != null && currentDeep < maxDeep) {
			if (deepCause == th) {
				final Throwable rootCause = ExceptionUtils.getRootCause(deepCause);
				if (rootCause == null) {
					return deepCause;
				}
				return rootCause;
			}
			deepCause = deepCause.getCause();
			currentDeep++;
		}
		return null;
	}

	private static String getStackTraceWithoutCircularReference(Throwable th, Throwable circularReferenceCause) {
		final StringBuilder sb = new StringBuilder(CIRCULAR_REFERENCE_MESSAGE_ON_DETECTION);
		sb.append("\n");
		sb.append(getFormattedMessage(th));
		Throwable deepCause = th.getCause();
		while (deepCause != null) {
			sb.append("\nCaused by: ");
			sb.append(getFormattedMessage(deepCause));
			if (deepCause == circularReferenceCause) {
				return sb.toString();
			}
			deepCause = deepCause.getCause();
		}
		return sb.toString();
	}

	private static String getFormattedMessage(Throwable th) {
		final StringBuilder sb = new StringBuilder(th.toString());
		sb.append("\n\tat ");
		sb.append(th.getStackTrace()[0].getClassName());
		sb.append(".");
		sb.append(th.getStackTrace()[0].getMethodName());
		sb.append("(");
		sb.append(th.getStackTrace()[0].getFileName());
		sb.append(":");
		sb.append(th.getStackTrace()[0].getLineNumber());
		sb.append(")");
		return sb.toString();
	}

}