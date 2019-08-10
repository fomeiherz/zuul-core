package top.fomeiherz.zuul.filters;

import top.fomeiherz.zuul.common.ZuulException;

public interface IZuulFilter {

    /**
     * a "true" return from this method means that the run() method should be invoked
     *
     * @return true if the run() method should be invoked. false will not invoke the run() method
     */
    boolean shouldFilter();

    /**
     * if shouldFilter() is true, this method will be invoked. this method is the core method of a ZuulFilter
     *
     * @return Some arbitrary artifact may be returned. Current implementation ignores it.
     */
    Object run() throws ZuulException;

}
