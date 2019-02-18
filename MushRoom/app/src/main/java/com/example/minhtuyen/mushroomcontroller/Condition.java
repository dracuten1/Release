package com.example.minhtuyen.mushroomcontroller;

import java.io.Serializable;

public class Condition implements Serializable {
    private String name;
    private int id;
    private int fHour;
    private int fMinute;
    private int tHour;
    private  int tMinute;
    private  float temp;
    private  float humi;
    private  int lux;
    private   int R;
    private int G;
    private int B;

    public Condition(String name) {
        this.name=name;
        this.fHour = 0;
        this.fMinute =0;
        this.tHour = 0;
        this.tMinute = 0;
        this.temp = 0;
        this.humi = 0;
        this.lux = 0;
        R = 0;
        G = 0;
        B = 0;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getfHour() {
        return fHour;
    }

    public void setfHour(int fHour) {
        this.fHour = fHour;
    }

    public int getfMinute() {
        return fMinute;
    }

    public void setfMinute(int fMinute) {
        this.fMinute = fMinute;
    }

    public int gettHour() {
        return tHour;
    }

    public void settHour(int tHour) {
        this.tHour = tHour;
    }

    public int gettMinute() {
        return tMinute;
    }

    public void settMinute(int tMinute) {
        this.tMinute = tMinute;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getHumi() {
        return humi;
    }

    public void setHumi(float humi) {
        this.humi = humi;
    }

    public int getLux() {
        return lux;
    }

    public void setLux(int lux) {
        this.lux = lux;
    }

    public int getR() {
        return R;
    }

    public void setR(int r) {
        R = r;
    }

    public int getG() {
        return G;
    }

    public void setG(int g) {
        G = g;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }

    @Override
    public String toString() {
        String str="";
        str+=String.valueOf(fHour)+":";
        str+=String.valueOf(fMinute)+"-";
        str+=String.valueOf(tHour)+":";
        str+=String.valueOf(tMinute)+"|";
        str+=String.valueOf(temp)+"/";
        str+=String.valueOf(humi)+"/";
        str+=String.valueOf(lux)+"/";
        str+=String.valueOf(R)+" ";
        str+=String.valueOf(G)+" ";
        str+=String.valueOf(B);
        //hh:mm-hh:mm|tmep/humi/lux/R G B
        return str;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
