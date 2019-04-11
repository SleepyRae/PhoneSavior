package com.example.inspiron.phonesavior.ui;

public class AppSet {
    final public static int TIP = 0;
    final public static int SLEEP = 1;
    final public static int REBOOT = 2;
    final public static int SHUTDOWN = 3;
    final public static int CANCEL = 4;

    private String label;
    private int type;
    private String time;

    public AppSet(String label, int type, String time) {
        this.label = label;
        this.type = type;
        this.time = time;
    }

    public AppSet() {

    }

    public String getLabel() {
        return label;
    }

    public int getType() {
        return type;
    }

    public String getTime() {
        return time;
    }
}
