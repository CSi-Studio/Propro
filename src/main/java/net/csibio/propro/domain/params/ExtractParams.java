package net.csibio.propro.domain.params;

import lombok.Data;

@Data
public class ExtractParams {

    Float mzExtractWindow;

    Float rtExtractWindow;

    //是否使用自适应聚合算法,推荐仅在Orbitrap的仪器上适合使用
    Boolean useAdaptiveWindow = false;

    Boolean isPpm = false;

    Float halfMzWindow;

    Float halfRtWindow;

    Float shapeScoreThreshold;

    public ExtractParams(Float mzExtractWindow, Float rtExtractWindow) {
        this.mzExtractWindow = mzExtractWindow;
        this.rtExtractWindow = rtExtractWindow;

        if (mzExtractWindow == -1) {
            useAdaptiveWindow = true;
        }

        if (mzExtractWindow > 1) {
            isPpm = true;
            halfMzWindow = (mzExtractWindow / 2f) * 1E-6f;
        } else {
            halfMzWindow = mzExtractWindow / 2f;
        }
    }

    @Override
    public String toString() {
        return "MZ Window:" + mzExtractWindow + ";RT Window:" + rtExtractWindow;
    }
}
