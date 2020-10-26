package com.csi.propro.test.algorithm;

import com.csi.propro.utils.CompressUtil;
import com.csi.propro.test.BaseTest;
import org.junit.Test;

public class AirdCompressorTest extends BaseTest {

    @Test
    public void testSortedIntegerCompress(){

        int[] testArray = new int[10];
        for(int i=0;i<testArray.length;i++){
            testArray[i] = i*3;
        }

        testArray = CompressUtil.compressForSortedInt(testArray);
        testArray = CompressUtil.decompressForSortedInt(testArray);
        System.out.println(testArray.length);
        for(int j =0;j<testArray.length;j++){
            assert testArray[j] == j*3;
        }
    }
}
