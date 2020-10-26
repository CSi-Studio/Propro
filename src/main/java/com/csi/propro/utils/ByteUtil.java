package com.csi.propro.utils;

import java.nio.ByteOrder;

public class ByteUtil {

    public static ByteOrder getByteOrder(String byteOrder){
        if("LITTLE_ENDIAN".equals(byteOrder)){
            return ByteOrder.LITTLE_ENDIAN;
        }else{
            return ByteOrder.BIG_ENDIAN;
        }
    }
}
