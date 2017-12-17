package com.ouyang.util;

import java.util.UUID;

/**
 * Created by Ouyang on 2017/7/6.
 */
public class UUIDUtil {
    public static String getId() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
