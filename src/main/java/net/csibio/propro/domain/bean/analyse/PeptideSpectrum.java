package net.csibio.propro.domain.bean.analyse;

import lombok.Data;

import java.util.HashMap;

/**
 * Created by Nico Wang
 * Time: 2018-12-25 14:25
 */
@Data
public class PeptideSpectrum {

    public Double[] rtArray;

    public HashMap<String, Double[]> intensitiesMap;

    public PeptideSpectrum(Double[] rtArray, HashMap<String, Double[]> intensitiesMap){
        this.rtArray = rtArray;
        this.intensitiesMap = intensitiesMap;
    }
}
