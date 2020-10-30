package net.csibio.propro.domain.bean.score;

import lombok.Data;

@Data
public class IntegrateWindowMzIntensity {

    double mz = -1;

    double intensity = 0;

    boolean signalFound = false;

    public IntegrateWindowMzIntensity(){}

    public IntegrateWindowMzIntensity(boolean signalFound){
        this.signalFound = signalFound;
    }
}
