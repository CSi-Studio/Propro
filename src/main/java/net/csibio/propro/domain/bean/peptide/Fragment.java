package net.csibio.propro.domain.bean.peptide;

import lombok.Data;

import java.util.HashMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 20:27
 */
@Data
public class Fragment implements Comparable<Fragment>{

    String transitionId;

    String sequence;

    String type;

    int location;

    int charge;

    int adjust;

    double deviation = 0.00;

    double monoMz;

    double averageMz;

    boolean isIsotope = false;

    //用于统计碎片出现的次数,仅在统计时有效
    int count = 1;

    //离子片段在原肽段中的起始位置,位置从0开始算,闭区间
    int start = 0;
    //离子片段在元肽段中的结束位置,位置从0开始算,闭区间
    int end = 0;

    HashMap<Integer,String> unimodMap;

    public Fragment(){}

    public Fragment(String transitionId){
        this.transitionId = transitionId;
    }

    public void count(){
        this.count++;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }

        if(obj instanceof Fragment){
            Fragment fragment = (Fragment) obj;
            if(sequence == null || type == null || fragment.getSequence() == null || fragment.getType() == null){
                return false;
            }

            return (this.sequence.equals(fragment.getSequence()) && this.type.equals(fragment.getType()));
        }else{
            return false;
        }

    }

    @Override
    public int hashCode() {
        return (sequence+type).hashCode();
    }

    @Override
    public int compareTo(Fragment o) {
        if(o == null){
            return 1;
        }
        if(this.getCount() > o.getCount()){
            return -1;
        }else if(this.getCount() == o.getCount()){
            return 0;
        }else{
            return 1;
        }
    }

//    @Override
//    public int compareTo(Fragment fragment) {
//        if(this.count > fragment.count){
//            return 1;
//        }else{
//            return -1;
//        }
//    }
}
