package com.yupi.cli.pattern;

public class Device {
    private String name;

    public Device(String name) {
        this.name = name;
    }
    public void turnOff(){
        System.out.println(name+"设备关闭");
    }

    public void turnOn(){
        System.out.println(name+"设备打开");
    }
}
