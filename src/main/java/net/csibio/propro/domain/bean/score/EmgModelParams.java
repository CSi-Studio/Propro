package net.csibio.propro.domain.bean.score;

import lombok.Data;

/**
 * Created by Nico Wang Ruimin
 * Time: 2018-08-19 19:13
 */
@Data
public class EmgModelParams {
    double boundingBoxMin;

    double boundingBoxMax;

    double mean = 1.0d;

    double variance = 1.0d;

    double height;

    double width;

    double symmetry;

    double retention;

    double toleranceStdevBox = 3.0d;

    double interpolationStep = 0.2d;
}
