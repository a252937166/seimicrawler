package com.ouyang.main;

import com.ouyang.util.UUIDUtil;
import com.qiniu.util.Auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Package: com.ouyang.main
 *
 * @Author: Ouyang
 * @Date: 2017/12/18
 */
public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String fileName = "null第1页.jpg";
        String domainOfBucket = "https://comic.ouyanglol.com";
        String encodedFileName = URLEncoder.encode(fileName, "utf-8");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName)+"-auth";
        String accessKey = "0j0DJNycJ8YTqLlgh22aKM57vfeAnfsuQy8ZrBDP";
        String secretKey = "_mj_dSZo93IGCjZ3JpusTNGWDMjfslfgZFWhnNSw";
        Auth auth = Auth.create(accessKey, secretKey);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        System.out.println(finalUrl);
    }
}
