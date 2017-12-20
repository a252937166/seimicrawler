package com.ouyang.main;

import com.ouyang.util.UUIDUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Package: com.ouyang.main
 *
 * @Author: Ouyang
 * @Date: 2017/12/18
 */
public class Test {
    public static void main(String[] args) {
        String a = "火影忍者303话[佐井的背叛]";
        Pattern pattern = Pattern.compile("^火影忍者(\\d+)话");
        Pattern pattern1 = Pattern.compile("\\[(.+)\\]");
        Matcher matcher = pattern.matcher(a);
        Matcher matcher2 = pattern1.matcher(a);
        if (matcher.find()) {
            System.out.println(Integer.valueOf(matcher.group(1)));
        }
        if (matcher2.find()) {
            System.out.println(matcher2.group(1));
        }
//        System.out.println(matcher2.replaceAll(""));
        System.out.println(UUIDUtil.getId());
        System.out.println(UUIDUtil.getId());
    }
}
