package top.fomeiherz.zuul.common;

/**
 * All handled exceptions in Zuul are ZuulExceptions
 */
public class ZuulException extends Exception {

    private String errorCause;
    private int statusCode = 500;

    /**
     * Source Throwable, message, status code and info about the cause
     * @param throwable
     * @param sMessage
     * @param statusCode
     * @param errorCause
     */
    public ZuulException(Throwable throwable, String sMessage, int statusCode, String errorCause) {
        super(sMessage, throwable);
        this.statusCode = statusCode;
        this.errorCause = errorCause;
    }

    /**
     * error message, status code and info about the cause
     * @param sMessage
     * @param statusCode
     * @param errorCause
     */
    public ZuulException(String sMessage, int statusCode, String errorCause) {
        super(sMessage);
        this.statusCode = statusCode;
        this.errorCause = errorCause;
    }

    /**
     * Source Throwable,  status code and info about the cause
     * @param throwable
     * @param statusCode
     * @param errorCause
     */
    public ZuulException(Throwable throwable, int statusCode, String errorCause) {
        super(throwable.getMessage(), throwable);
        this.statusCode = statusCode;
        this.errorCause = errorCause;
    }

}
