package com.silabs.thunderboard.common.log;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class ApplicationDebugTree  extends Timber.DebugTree {
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");
    private static final ThreadLocal<String> NEXT_TAG = new ThreadLocal<String>();

    private final String applicationTag;

    public ApplicationDebugTree(String tag) {
        applicationTag = tag;
    }

    private static String createTag() {
        String tag = NEXT_TAG.get();
        if (tag != null) {
            NEXT_TAG.remove();
            return tag;
        }

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();

        if (stackTrace.length < 6) {
            throw new IllegalStateException(
                    "Synthetic stacktrace didn't have enough elements: are you using proguard?");
        }

        StackTraceElement stackTraceElement = stackTrace[5];
        tag = stackTraceElement.getClassName();
        Matcher m = ANONYMOUS_CLASS.matcher(tag);
        if (m.find()) {
            tag = m.replaceAll("");
        }

        tag = String.format("%20s %20s", tag.substring(tag.lastIndexOf('.') + 1), stackTraceElement.getMethodName());
        return tag.substring(tag.lastIndexOf('.') + 1);
    }

    static String formatString(String message, Object... args) {
        // If no varargs are supplied, treat it as a request to log the string without formatting.
        return args.length == 0 ? message : String.format(message, args);
    }

    @Override
    public void v(String message, Object... args) {
        throwShade(Log.VERBOSE, formatString(message, args), null);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        throwShade(Log.VERBOSE, formatString(message, args), t);
    }

    @Override
    public void d(String message, Object... args) {
        throwShade(Log.DEBUG, formatString(message, args), null);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        throwShade(Log.DEBUG, formatString(message, args), t);
    }

    @Override
    public void i(String message, Object... args) {
        throwShade(Log.INFO, formatString(message, args), null);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        throwShade(Log.INFO, formatString(message, args), t);
    }

    @Override
    public void w(String message, Object... args) {
        throwShade(Log.WARN, formatString(message, args), null);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        throwShade(Log.WARN, formatString(message, args), t);
    }

    @Override
    public void e(String message, Object... args) {
        throwShade(Log.ERROR, formatString(message, args), null);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        throwShade(Log.ERROR, formatString(message, args), t);
    }

    private void throwShade(int priority, String message, Throwable t) {
        if (message == null || message.length() == 0) {
            if (t != null) {
                message = Log.getStackTraceString(t);
            } else {
                // Swallow message if it's null and there's no throwable.
                return;
            }
        } else if (t != null) {
            message += "\n" + Log.getStackTraceString(t);
        }

        String tag = createTag();
        if (message.length() < 4000) {
            /*
             * Replacing the original tag with the application tag and
             * using the tag for the class formatted at the beginning.
             */
            String taggedMessage = String.format("%s -> %s", tag, message);
            Log.println(priority, applicationTag, taggedMessage);
        } else {
            // It's rare that the message will be this large, so we're ok with the perf hit of splitting
            // and calling Log.println N times.  It's possible but unlikely that a single line will be
            // longer than 4000 characters: we're explicitly ignoring this case here.
            String[] lines = message.split("\n");
            for (String line : lines) {
                Log.println(priority, tag, line);
            }
        }
    }
}