package com.westlake.air.swathplatform.domain.bean;

import lombok.Data;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-17 10:16
 */
@Data
public class TargetTransition implements Comparable<TargetTransition>{

    Boolean isMS1;

    //对应的transition的Id,如果是MS1的则为对应的第一条transition的Id(一个MS1会对应多条transition记录)
    String id;

    //对应的MS1荷质比
    Double precursorMz;

    //对应的MS2荷质比
    Double productMz;

    //对应的肽段全名(包含unimod)
    String fullName;

    Double rt;

    Double rtStart;

    Double rtEnd;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof TargetTransition) {
            TargetTransition target = (TargetTransition) obj;
            if (this.fullName.equals(target.getFullName())) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (isMS1+fullName).hashCode();
    }

    @Override
    public int compareTo(TargetTransition o) {
        if (o == null) {
            return 1;
        }
        if(this.isMS1.equals(o.getIsMS1()) && this.isMS1){
            if(this.precursorMz > o.getPrecursorMz()){
                return -1;
            } else if(this.precursorMz.equals(o.getPrecursorMz())){
                return 0;
            } else{
                return 1;
            }

        } else if(this.isMS1.equals(o.getIsMS1()) && !this.isMS1){
            if(this.productMz > o.getProductMz()){
                return -1;
            } else if(this.productMz.equals(o.getProductMz())){
                return 0;
            } else{
                return 1;
            }
        } else if(this.isMS1){
            return 1;
        } else{
            return -1;
        }
    }
}