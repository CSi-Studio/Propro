package com.westlake.air.propro.domain.bean.score;

import lombok.Data;

import java.util.List;

@Data
public class BaseScores {

    Double[] scores;

    public void put(String typeName, Double score, List<String> scoreTypes) {
        int index = scoreTypes.indexOf(typeName);
        if (index != -1) {
            scores[index] = score;
        }
    }

    public Double get(String typeName, List<String> scoreTypes) {
        int index = scoreTypes.indexOf(typeName);
        if (scores == null || index == -1) {
            return null;
        } else {
            Double d = scores[index];
            return d == null ? 0d : d;
        }
    }

    //所谓的Remove是不会真正的Remove掉相关的分数,只是将对应的分数置为null
    public void remove(String typeName, List<String> scoreTypes) {
        int index = scoreTypes.indexOf(typeName);
        if (scores != null && index != -1) {
            scores[index] = null;
        }
    }
}
