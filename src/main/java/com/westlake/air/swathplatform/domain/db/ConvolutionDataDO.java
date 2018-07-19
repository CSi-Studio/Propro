package com.westlake.air.swathplatform.domain.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.TreeMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 15:48
 */
@Data
@Document(collection = "convolutionData")
public class ConvolutionDataDO {

    @Id
    String id;

    @Indexed
    String expId;

    String transitionId;

    Integer msLevel;

    Double mz;

    TreeMap<Double, Double> rtIntensityMap;
}