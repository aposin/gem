package org.aposin.gem.core.exception;

/**
 * GEM exception for a fatal error.
 */
public final class GemFatalException extends GemException {

    /**
     * Default constructor.
     * 
     * @param msg detail exception message.
     * @param cause underlying cause of the exception (if any).
     */
    public GemFatalException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates an exception without cause.
     * 
     * @param msg detail exception message.
     */
    public GemFatalException(String msg) {
        super(msg);
    }

    public static GemFatalException from(final GemException e) {
        return new GemFatalException(e.getLocalizedMessage(), e.getCause());
    }

}
