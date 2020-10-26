package com.csi.propro.domain.bean.analyse;

import lombok.Data;

import java.util.HashMap;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-29 19:35
 */
@Data
public class RtIntensitiesDouble {

    Double[] rtArray;

    HashMap<String, Double[]> intensitiesMap;

    public RtIntensitiesDouble() {
    }

    public RtIntensitiesDouble(Float[] rtArray, String cutInfo, Float[] intensityArray) {

        Double[] rts = new Double[rtArray.length];
        Double[] ints = new Double[intensityArray.length];
        for (int i = 0; i < rts.length; i++) {
            rts[i] = Double.parseDouble(rtArray[i].toString());
            ints[i] = Double.parseDouble(intensityArray[i].toString());
        }
        this.rtArray = rts;
        this.intensitiesMap = new HashMap<>();
        this.intensitiesMap.put(cutInfo, ints);
    }

    public RtIntensitiesDouble(Double[] rtArray,String cutInfo, Double[] intensityArray) {
        this.rtArray = rtArray;
        this.intensitiesMap = new HashMap<>();
        this.intensitiesMap.put(cutInfo, intensityArray);
    }

    public RtIntensitiesDouble(Float[] rtArray, HashMap<String, Float[]> intensitiesMap) {
        this.rtArray = new Double[rtArray.length];
        this.intensitiesMap = new HashMap<>();
        for (String cutInfo : intensitiesMap.keySet()) {
            this.intensitiesMap.put(cutInfo, new Double[rtArray.length]);
        }

        for (int i = 0; i < rtArray.length; i++) {
            this.rtArray[i] = Double.parseDouble(rtArray[i].toString());
            for (String cutInfo : intensitiesMap.keySet()) {
                this.intensitiesMap.get(cutInfo)[i] = Double.parseDouble(intensitiesMap.get(cutInfo)[i].toString());
            }
        }
    }
}
