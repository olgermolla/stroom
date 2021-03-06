package stroom.node.impl;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import stroom.util.shared.IsConfig;

import javax.inject.Singleton;

@Singleton
public class HeapHistogramConfig implements IsConfig {

    private String classNameMatchRegex = "^stroom\\..*$";
    private String classNameReplacementRegex = "((?<=\\$Proxy)[0-9]+|(?<=\\$\\$)[0-9a-f]+|(?<=\\$\\$Lambda\\$)[0-9]+\\/[0-9]+)";
    private String jMapExecutable = "jmap";

    @JsonPropertyDescription("A single regex that will be used to filter classes from the heap histogram internal statistic based on their name. e.g '^(stroom\\..*)$'. If no value is supplied all classes will be included. If a value is supplied only those class names matching the regex will be included.")
    public String getClassNameMatchRegex() {
        return classNameMatchRegex;
    }

    public void setClassNameMatchRegex(final String classNameMatchRegex) {
        this.classNameMatchRegex = classNameMatchRegex;
    }

    @JsonPropertyDescription("A single regex that will be used to replace all matches in the class name with '--REPLACED--'. This is to prevent ids for anonymous inner classes and lambdas from being included in the class name. E.g '....DocRefResourceHttpClient$$Lambda$46/1402766141' becomes '....DocRefResourceHttpClient$$Lambda$--REPLACED--'. ")
    public String getClassNameReplacementRegex() {
        return classNameReplacementRegex;
    }

    public void setClassNameReplacementRegex(final String classNameReplacementRegex) {
        this.classNameReplacementRegex = classNameReplacementRegex;
    }
}
