package net.csibio.propro.test.util;

import net.csibio.propro.utils.MathUtil;
import net.csibio.propro.test.BaseTest;
import org.junit.Test;

public class MathUtilTest extends BaseTest {

    @Test
    public void countNumPositives_test1(){
        Double[] d = new Double[6];
        d[0] = 4.0d;
        d[1] = 4.0d;
        d[2] = 3.0d;
        d[3] = 2.0d;
        d[4] = 2.0d;
        d[5] = 1.0d;
        int[] a = MathUtil.countNumPositives(d);
        assert a[0] == 6;
        assert a[1] == 6;
        assert a[2] == 4;
        assert a[3] == 3;
        assert a[4] == 3;
        assert a[5] == 1;
    }
}
