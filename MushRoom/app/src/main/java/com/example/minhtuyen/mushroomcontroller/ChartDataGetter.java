package com.example.minhtuyen.mushroomcontroller;

import java.util.ArrayList;

public class ChartDataGetter {
    private ArrayList<String> timeData=new ArrayList<>();
    private ArrayList<Float> tempData=new ArrayList<>();
    private ArrayList<Float> humiData=new ArrayList<>();
    private ArrayList<Integer> lightData=new ArrayList<>();
    public ChartDataGetter(String s){
        s = s.replace("\n", "").replace("\r", "");
        String[] datas = s.split("\\-");
        for ( String data: datas) {
            String[] line=data.split("\\|");
            if(line.length==4){
                timeData.add(line[0]);
                tempData.add(Float.parseFloat(line[1]));
                humiData.add(Float.parseFloat(line[2]));
                lightData.add(Integer.parseInt(line[3]));
            }
        }
    }

    public ArrayList<Float> getHumiData() {
        return humiData;
    }

    public ArrayList<Float> getTempData() {
        return tempData;
    }

    public ArrayList<String> getTimeData() {
        return timeData;
    }

    public ArrayList<Integer> getLightData() {
        return lightData;
    }
}
