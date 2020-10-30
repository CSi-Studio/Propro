package net.csibio.propro.domain.bean.analyse;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-29 19:35
 */
@Data
public class RtIntensityPairsDouble {
    Double[] rtArray;

    Double[] intensityArray;

    public RtIntensityPairsDouble(){}

    public RtIntensityPairsDouble(Float[] rtArray, Float[] intensityArray){
        Double[] rts = new Double[rtArray.length];
        Double[] ints = new Double[intensityArray.length];
        for(int i=0; i<rts.length; i++){
            rts[i] = Double.parseDouble(rtArray[i].toString());
            ints[i] = Double.parseDouble(intensityArray[i].toString());
        }
        this.rtArray = rts;
        this.intensityArray = ints;
    }

    public RtIntensityPairsDouble(Double[] rtArray, Double[] intensityArray){

        this.rtArray = rtArray;
        this.intensityArray = intensityArray;
    }

    public RtIntensityPairsDouble(RtIntensityPairs rtIntensityPairs){
        Double[] rt = new Double[rtIntensityPairs.getRtArray().length];
        for(int i=0; i<rt.length; i++){
            rt[i] = (double) rtIntensityPairs.getRtArray()[i];
        }
        this.rtArray = rt;
        Double[] intensity = new Double[rtIntensityPairs.getIntensityArray().length];
        for(int i=0; i<intensity.length; i++){
            intensity[i] = (double) rtIntensityPairs.getIntensityArray()[i];
        }
        this.intensityArray = intensity;
    }

    public RtIntensityPairsDouble(RtIntensityPairsDouble rtIntensityPairsDouble){
        this.rtArray = rtIntensityPairsDouble.getRtArray().clone();
        this.intensityArray = rtIntensityPairsDouble.getIntensityArray().clone();
    }
}
