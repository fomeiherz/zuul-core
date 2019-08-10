package top.fomeiherz.zuul.common;

import java.io.File;

public interface IDynamicCodeCompiler {

    Class compile(String sCode, String sName) throws Exception;

    Class compile(File file) throws Exception;

}
