package top.fomeiherz.zuul.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fomeiherz.zuul.common.ExecutionStatus;
import top.fomeiherz.zuul.common.ZuulException;
import top.fomeiherz.zuul.common.ZuulFilterResult;
import top.fomeiherz.zuul.context.RequestContext;
import top.fomeiherz.zuul.filters.ZuulFilter;

import java.util.List;

public class FilterProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FilterProcessor.class);

    private static FilterProcessor instance = new FilterProcessor();

    /**
     * @return the singleton FilterProcessor
     */
    public static FilterProcessor getInstance() {
        return instance;
    }

    /**
     * sets a singleton processor in case of a need to override default behavior
     *
     * @param processor
     */
    public static void setProcessor(FilterProcessor processor) {
        instance = processor;
    }

    /**
     * runs "post" filters which are called after "route" filters.
     * ZuulExceptions from ZuulFilters are thrown. Any other Throwables are
     * caught and a ZuulException is thrown out with a 500 status code
     *
     * @throws ZuulException
     */
    public void postRoute() throws ZuulException {
        try {
            runFilters("post");
        } catch (Throwable e) {
            if (e instanceof ZuulException) {
                throw (ZuulException) e;
            }
            throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_POST_FILTER_" + e.getClass().getName());
        }

    }

    /**
     * runs all "error" filters. These are called only if an exception occurs.
     * Exceptions from this are swallowed and logged so as not to bubble up.
     *
     * @throws ZuulException
     */
    public void error() throws ZuulException {
        try {
            runFilters("error");
        } catch (Throwable e) {
            if (e instanceof ZuulException) {
                throw (ZuulException) e;
            }
            throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_POST_FILTER_" + e.getClass().getName());
        }
    }

    /**
     * Runs all "route" filters. These filters route calls to an origin.
     *
     * @throws ZuulException if an exception occurs.
     */
    public void route() throws ZuulException {
        try {
            runFilters("route");
        } catch (Throwable e) {
            if (e instanceof ZuulException) {
                throw (ZuulException) e;
            }
            throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_ROUTE_FILTER_" + e.getClass().getName());
        }
    }

    /**
     * runs all "pre" filters. These filters are run before routing to the
     * orgin.
     *
     * @throws ZuulException
     */
    public void preRoute() throws ZuulException {
        try {
            runFilters("pre");
        } catch (Throwable e) {
            if (e instanceof ZuulException) {
                throw (ZuulException) e;
            }
            throw new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_IN_PRE_FILTER_" + e.getClass().getName());
        }
    }

    /**
     * runs all filters of the filterType sType/ Use this method within filters
     * to run custom filters by type
     *
     * @param sType the filterType.
     * @return
     * @throws Throwable throws up an arbitrary exception
     */
    public Object runFilters(String sType) throws Throwable {
        boolean bResult = false;
        List<ZuulFilter> list = FilterLoader.getInstance().getFiltersByType(sType);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                ZuulFilter zuulFilter = list.get(i);
                Object result = processZuulFilter(zuulFilter);
                if (result != null && result instanceof Boolean) {
                    bResult |= ((Boolean) result);
                }
            }
        }
        return bResult;
    }

    /**
     * Processes an individual ZuulFilter. This method adds Debug information.
     * Any uncaught Thowables are caught by this method and converted to a
     * ZuulException with a 500 status code.
     *
     * @param filter
     * @return the return value for that filter
     * @throws ZuulException
     */
    public Object processZuulFilter(ZuulFilter filter) throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        boolean bDebug = ctx.debugRouting();
        long execTime = 0;
        String filterName = "";

        try {
            long ltime = System.currentTimeMillis();
            filterName = filter.getClass().getSimpleName();

            Object o = null;
            Throwable t = null;

            ZuulFilterResult result = filter.runFilter();
            ExecutionStatus s = result.getStatus();
            execTime = System.currentTimeMillis() - ltime;

            switch (s) {
                case FAILED:
                    t = result.getException();
                    ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                    break;
                case SUCCESS:
                    o = result.getResult();
                    ctx.addFilterExecutionSummary(filterName, ExecutionStatus.SUCCESS.name(), execTime);
                    break;
                default:
                    break;
            }
            if (t != null) throw t;

            return o;
        } catch (Throwable e) {
            if (e instanceof ZuulException) {
                throw (ZuulException) e;
            } else {
                ZuulException ex = new ZuulException(e, "Filter threw Exception", 500, filter.filterType() + ":" + filterName);
                ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                throw ex;
            }
        }

    }

}

