package org.ufba.raide.java.testsmell.detector.smell;

import java.util.Objects;

public class MethodUsage {
    private String testMethodName, productionMethodName, range;

    public MethodUsage(String testMethod, String productionMethod, String range) {
        this.testMethodName = testMethod;
        this.productionMethodName = productionMethod;
        this.range = range;
    }

    public MethodUsage(String testMethod){
        this.testMethodName = testMethod;
    }

    public MethodUsage (String testMethod, String productionMethod) {
        this.testMethodName = testMethod;
        this.productionMethodName = productionMethod;
    }

    public String getRange () {
        return range;
    }

    public String getProductionMethodName() {
        return productionMethodName;
    }

    public String getTestMethodName() {
        return testMethodName;
    }

    public String getBlock() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodUsage that = (MethodUsage) o;
        return Objects.equals(testMethodName, that.testMethodName) &&
                Objects.equals(productionMethodName, that.productionMethodName) &&
                Objects.equals(range, that.range);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testMethodName, productionMethodName, range);
    }
}