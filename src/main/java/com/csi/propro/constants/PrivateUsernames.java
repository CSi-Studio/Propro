package com.csi.propro.constants;

import java.util.ArrayList;
import java.util.List;

public class PrivateUsernames {

    public static String NAME_PUBLIC = "public";
    public static String NAME_OFFICIAL = "official";
    public static List<String> privates = new ArrayList<>();

    static{
        privates.add(NAME_PUBLIC);
        privates.add(NAME_OFFICIAL);
    }


}
