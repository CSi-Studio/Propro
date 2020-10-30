package net.csibio.propro.domain.bean.score;

import lombok.Data;

/**
 * Created by Nico Wang
 * Time: 2019-01-08 20:54
 */
@Data
public class IonPeak {

    double intensity;
    int leftRtIndex;
    int rightRtIndex;
    int apexRtIndex;
    int index;

    public IonPeak(double intensity, int leftRt, int rightRt, int apexRtIndex, int index){
        this.intensity = intensity;
        this.leftRtIndex = leftRt;
        this.rightRtIndex = rightRt;
        this.apexRtIndex = apexRtIndex;
        this.index = index;
    }
}
