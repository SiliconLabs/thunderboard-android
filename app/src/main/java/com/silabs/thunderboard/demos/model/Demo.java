package com.silabs.thunderboard.demos.model;

public class Demo {
    public String demoName;
    public int demoImageResource;
    public Class<?> demoClass;
    public boolean demoEnabled;
    public String demoDescription;

    public Demo(String demoName, int demoImageResource, Class<?> demoClass, boolean demoEnabled, String demoDescription) {
        this.demoName = demoName;
        this.demoImageResource = demoImageResource;
        this.demoClass = demoClass;
        this.demoEnabled = demoEnabled;
        this.demoDescription = demoDescription;
    }
}
