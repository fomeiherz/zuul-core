package top.fomeiherz.zuul.groovy;

import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.fomeiherz.zuul.common.IDynamicCodeCompiler;

import java.io.File;
import java.io.IOException;

/**
 * Groovy code compiler
 */
public class GroovyCompiler implements IDynamicCodeCompiler {

    private static final Logger logger = LoggerFactory.getLogger(GroovyCompiler.class);

    /**
     * Compiles Groovy code and returns the Class of the compiles code.
     *
     * @param sCode
     * @param sName
     * @return
     */
    public Class compile(String sCode, String sName) {
        GroovyClassLoader loader = getGroovyClassLoader();
        logger.warn("Compiling filter: " + sName);
        Class groovyClass = loader.parseClass(sCode, sName);
        return groovyClass;
    }

    /**
     * @return a new GroovyClassLoader
     */
    GroovyClassLoader getGroovyClassLoader() {
        return new GroovyClassLoader();
    }

    /**
     * Compiles groovy class from a file
     *
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public Class compile(File file) throws IOException {
        GroovyClassLoader loader = getGroovyClassLoader();
        Class groovyClass = loader.parseClass(file);
        return groovyClass;
    }

}
