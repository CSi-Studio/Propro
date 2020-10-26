package com.csi.propro.domain.bean.math;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-14 22:02
 */
@Data
public class BisectionLowHigh {

    int low;

    int high;

    public BisectionLowHigh(int low, int high){
        this.low = low;
        this.high = high;
    }

    public BisectionLowHigh(){
    }

}
