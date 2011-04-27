package com.bloatit.common;

import org.apache.log4j.Logger;

// TRACE
// DEBUG
// INFO
// WARN -> not correct behavior but non so serious
// ERROR -> should be reported to the admin
// FATAL -> should be reported to the admin NOW !

public final class Log {

    public static interface LoggerInterface {
        void fatal(final Object message);

        void fatal(final Object message, final Throwable e);

        void error(final Object message);

        void error(final Object message, final Throwable e);

        void warn(final Object message);

        void warn(final Object message, final Throwable e);

        void info(final Object message);

        void info(final Object message, final Throwable e);

        void debug(final Object message);

        void debug(final Object message, final Throwable e);

        void trace(final Object message);

        void trace(final Object message, final Throwable e);
    }

    public static class BloatitLogger implements LoggerInterface {

        private final Logger log;

        private BloatitLogger(final Logger log) {
            super();
            this.log = log;
        }

        @Override
        public void fatal(final Object message) {
            log.fatal(message + getStackTrace());
        }

        private String getStackTrace() {
            final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            final StringBuilder sb = new StringBuilder();

            for (int i = 3; i < trace.length; i++) {
                sb.append("\n\t").append(trace[i].toString());
            }

            return sb.toString();
        }

        @Override
        public void fatal(final Object message, final Throwable e) {
            log.fatal(message, e);
        }

        @Override
        public void error(final Object message) {
            log.error(message + getStackTrace());
        }

        @Override
        public void error(final Object message, final Throwable e) {
            log.error(message, e);
        }

        @Override
        public void warn(final Object message) {
            log.warn(message);
        }

        @Override
        public void warn(final Object message, final Throwable e) {
            log.warn(message, e);
        }

        @Override
        public void info(final Object message) {
            log.info(message);
        }

        @Override
        public void info(final Object message, final Throwable e) {
            log.info(message, e);
        }

        @Override
        public void debug(final Object message) {
            log.debug(message);
        }

        @Override
        public void debug(final Object message, final Throwable e) {
            log.debug(message, e);
        }

        @Override
        public void trace(final Object message) {
            log.trace(message);
        }

        @Override
        public void trace(final Object message, final Throwable e) {
            log.trace(message, e);
        }
    }

    private Log() {
        // disactivate default ctor
    }

    private static final LoggerInterface MODEL = new BloatitLogger(Logger.getLogger("com.bloatit.model"));
    private static final LoggerInterface DATA = new BloatitLogger(Logger.getLogger("com.bloatit.data"));
    private static final LoggerInterface WEB = new BloatitLogger(Logger.getLogger("com.bloatit.web"));
    private static final LoggerInterface FRAMEWORK = new BloatitLogger(Logger.getLogger("com.bloatit.framework"));
    private static final LoggerInterface MAIL = new BloatitLogger(Logger.getLogger("com.bloatit.mail"));
    private static final LoggerInterface REST = new BloatitLogger(Logger.getLogger("com.bloatit.rest"));
    private static final LoggerInterface RESOURCE = new BloatitLogger(Logger.getLogger("com.bloatit.resource"));
    private static final LoggerInterface CACHE = new BloatitLogger(Logger.getLogger("com.bloatit.cache"));

    public static LoggerInterface model() {
        return MODEL;
    }

    public static LoggerInterface data() {
        return DATA;
    }

    public static LoggerInterface web() {
        return WEB;
    }

    public static LoggerInterface framework() {
        return FRAMEWORK;
    }

    public static LoggerInterface mail() {
        return MAIL;
    }

    public static LoggerInterface rest() {
        return REST;
    }

    public static LoggerInterface resources() {
        return RESOURCE;
    }

    public static LoggerInterface cache() {
        return CACHE;
    }

}
