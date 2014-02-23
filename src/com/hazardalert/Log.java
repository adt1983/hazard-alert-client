package com.hazardalert;

public class Log {
	private static String getTag() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
		String tag = ste.getFileName() + ":" + ste.getLineNumber();
		return tag;
	}

	private static String getMethodName() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
		String methodName = ste.getMethodName();
		return methodName;
	}

	public static void i() {
		if (BuildConfig.DEBUG) {
			android.util.Log.i(getTag(), getMethodName());
		}
	}

	public static void i(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.i(getTag(), getMethodName() + ": " + msg);
		}
	}

	public static void v() {
		if (BuildConfig.DEBUG) {
			android.util.Log.v(getTag(), getMethodName());
		}
	}

	public static void v(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.v(getTag(), getMethodName() + ": " + msg);
		}
	}

	public static void e() {
		if (BuildConfig.DEBUG) {
			android.util.Log.e(getTag(), getMethodName());
		}
	}

	public static void e(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.e(getTag(), getMethodName() + ": " + msg);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (BuildConfig.DEBUG) {
			android.util.Log.e(getTag(), getMethodName() + ": " + msg, tr);
		}
	}

	public static void d() {
		if (BuildConfig.DEBUG) {
			android.util.Log.d(getTag(), getMethodName());
		}
	}

	public static void d(String msg) {
		if (BuildConfig.DEBUG) {
			android.util.Log.d(getTag(), getMethodName() + ": " + msg);
		}
	}

	public static void d(String msg, Throwable tr) {
		if (BuildConfig.DEBUG) {
			android.util.Log.d(getTag(), getMethodName() + ": " + msg, tr);
		}
	}
}
