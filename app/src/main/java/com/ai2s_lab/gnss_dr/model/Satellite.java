package com.ai2s_lab.gnss_dr.model;

public class Satellite {
    private int id;
    private String type;
    private boolean is_used;
    private double elev;
    private double azim;
    private double cno;

    public Satellite(int id, String type, boolean is_used, double elev, double azim, double cno) {
        this.id = id;
        this.type = type;
        this.is_used = is_used;
        this.elev = elev;
        this.azim = azim;
        this.cno = cno;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public boolean isIs_used() {
        return is_used;
    }

    public double getElev() {
        return elev;
    }

    public double getAzim() {
        return azim;
    }

    public double getCno() {
        return cno;
    }
}
