package top.fomeiherz.zuul.core;

import top.fomeiherz.zuul.common.IDynamicCodeCompiler;
import top.fomeiherz.zuul.filters.DefaultFilterFactory;
import top.fomeiherz.zuul.filters.FilterRegistry;
import top.fomeiherz.zuul.filters.IFilterFactory;
import top.fomeiherz.zuul.filters.ZuulFilter;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is one of the core classes in Zuul. It compiles, loads from a File, and checks if source code changed.
 * It also holds ZuulFilters by filterType.
 */
public class FilterLoader {

    private final static FilterLoader instance = new FilterLoader();

    private static IDynamicCodeCompiler COMPILER;
    private static IFilterFactory FILTER_FACTORY = new DefaultFilterFactory();

    private FilterRegistry filterRegistry = FilterRegistry.instance();

    // <filterClass, modifyTime>
    private final ConcurrentHashMap<String, Long> filterClassLastModified = new ConcurrentHashMap<>();
    // <filterClass, classCode>
    private final ConcurrentHashMap<String, String> filterClassCode = new ConcurrentHashMap<>();
    // <type, List<ZuulFilter>>
    private final ConcurrentHashMap<String, List<ZuulFilter>> hashFiltersByType = new ConcurrentHashMap<>();

    public FilterLoader() {

    }

    /**
     * @return Singleton FilterLoader
     */
    public static FilterLoader getInstance() {
        return instance;
    }

    /**
     * Sets a Dynamic Code Compiler
     *
     * @param compiler
     */
    public void setCompiler(IDynamicCodeCompiler compiler) {
        COMPILER = compiler;
    }

    /**
     * Override by tests
     * @param r
     */
    public void setFilterRegistry(FilterRegistry r) {
        this.filterRegistry = r;
    }

    /**
     * Sets a FilterFactory
     *
     * @param factory
     */
    public void setFilterFactory(IFilterFactory factory) {
        FILTER_FACTORY = factory;
    }

    /**
     * From a file this will read the ZuulFilter source code, compile it, and add it to the list of current filters
     * a true response means that it was successful.
     *
     * @param file
     * @return true if the filter in file successfully read, compiled, verified and added to Zuul
     */
    public boolean putFilter(File file) throws Exception {
        String sName = file.getAbsolutePath() + file.getName();
        // if modified
        if (filterClassLastModified.get(sName) != null && file.lastModified() != filterClassLastModified.get(sName)) {
            filterRegistry.remove(sName);
        }
        ZuulFilter filter = filterRegistry.get(sName);
        // if yet loading
        if (filter == null) {
            Class clazz = COMPILER.compile(file);
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                filter = FILTER_FACTORY.newInstance(clazz);
                filterRegistry.put(sName, filter);
                filterClassLastModified.put(sName, file.lastModified());
                List<ZuulFilter> list = hashFiltersByType.get(filter.filterType());
                if (list != null) {
                    // rebuild this list
                    hashFiltersByType.remove(filter.filterType());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of filters by the filterType specified
     *
     * @param filterType
     * @return a List<ZuulFilter>
     */
    public List<ZuulFilter> getFiltersByType(String filterType) {
        List<ZuulFilter> list = hashFiltersByType.get(filterType);
        if (list != null) {
            return list;
        }
        list = new ArrayList<>();
        Collection<ZuulFilter> filters = filterRegistry.getAllFilters();
        for (Iterator<ZuulFilter> iterator = filters.iterator(); iterator.hasNext();) {
            ZuulFilter filter = iterator.next();
            if (filter.filterType().equals(filterType)) {
                list.add(filter);
            }
        }
        Collections.sort(list); // Sort by filter order

        hashFiltersByType.putIfAbsent(filterType, list);

        return list;
    }

}
