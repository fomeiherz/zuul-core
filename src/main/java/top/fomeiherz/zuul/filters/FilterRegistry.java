package top.fomeiherz.zuul.filters;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilterRegistry {

    private FilterRegistry() {
    }

    private static final FilterRegistry instance = new FilterRegistry();

    public static final FilterRegistry instance() {
        return instance;
    }

    // <filePathAndName, ZuulFilter>
    private final ConcurrentHashMap<String, ZuulFilter> filters = new ConcurrentHashMap<>();

    public ZuulFilter remove(String key) {
        return this.filters.remove(key);
    }

    public ZuulFilter get(String key) {
        return this.filters.get(key);
    }

    public void put(String key, ZuulFilter filter) {
        this.filters.putIfAbsent(key, filter);
    }

    public int size() {
        return this.filters.size();
    }

    public Collection<ZuulFilter> getAllFilters() {
        return this.filters.values();
    }


}
