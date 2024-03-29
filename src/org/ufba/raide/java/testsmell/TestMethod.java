package org.ufba.raide.java.testsmell;

import java.util.HashMap;
import java.util.Map;

public class TestMethod extends SmellyElement {

    private String methodName;
    private boolean hasSmell;
    private Map<String, String> data;

    private String range;

    public TestMethod(String methodName) {
        this.methodName = methodName;
        data = new HashMap<>();
    }

    public void setHasSmell(boolean hasSmell) {
        this.hasSmell = hasSmell;
    }

    public void addDataItem(String name, String value) {
        data.put(name, value);
    }

    @Override
    public String getElementName() {
        return methodName;
    }

    @Override
    public boolean getHasSmell() {
        return hasSmell;
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }
 
    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

}
