package com.silabs.thunderboard.demos.model;

public class Demo {
    public String demoName;
    public int demoImageResource;
    public Class<?> demoClass;
    public boolean demoEnabled;

    public Demo(String demoName, int demoImageResource, Class<?> demoClass, boolean demoEnabled) {
        this.demoName = demoName;
        this.demoImageResource = demoImageResource;
        this.demoClass = demoClass;
        this.demoEnabled = demoEnabled;
    }
}
