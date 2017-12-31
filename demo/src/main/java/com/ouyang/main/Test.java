package com.ouyang.main;

import com.ouyang.util.UUIDUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
//        String fileName = "null第1页.jpg";
//        String domainOfBucket = "https://comic.ouyanglol.com";
//        String encodedFileName = URLEncoder.encode(fileName, "utf-8");
//        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName)+"-auth";
//        String accessKey = "0j0DJNycJ8YTqLlgh22aKM57vfeAnfsuQy8ZrBDP";
//        String secretKey = "_mj_dSZo93IGCjZ3JpusTNGWDMjfslfgZFWhnNSw";
//        Auth auth = Auth.create(accessKey, secretKey);
//        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
//        String finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
//        System.out.println(finalUrl);
        test(test1());
    }


    public static void test(List<String> list) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释
        String accessKey = "0j0DJNycJ8YTqLlgh22aKM57vfeAnfsuQy8ZrBDP";
        String secretKey = "_mj_dSZo93IGCjZ3JpusTNGWDMjfslfgZFWhnNSw";
        String bucket = "comic";
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            //单次批量请求的文件数量不得超过1000
            String[] keyList = list.toArray(new String[list.size()]);
            BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
            batchOperations.addDeleteOp(bucket, keyList);
            Response response = bucketManager.batch(batchOperations);
            BatchStatus[] batchStatusList = response.jsonToObject(BatchStatus[].class);
            for (int i = 0; i < keyList.length; i++) {
                BatchStatus status = batchStatusList[i];
                String key = keyList[i];
                System.out.print(key + "\t");
                if (status.code == 200) {
                    System.out.println("delete success");
                } else {
                    System.out.println();
                }
            }
        } catch (QiniuException ex) {
            System.err.println(ex.response.toString());
        }
    }


    public static List<String> test1(){
        List<String> list = new ArrayList<>();
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释
        String accessKey = "0j0DJNycJ8YTqLlgh22aKM57vfeAnfsuQy8ZrBDP";
        String secretKey = "_mj_dSZo93IGCjZ3JpusTNGWDMjfslfgZFWhnNSw";
        String bucket = "comic";
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
//文件名前缀
        String prefix = "";
//每次迭代的长度限制，最大1000，推荐值 1000
        int limit = 1000;
//指定目录分隔符，列出所有公共前缀（模拟列出目录效果）。缺省值为空字符串
        String delimiter = "";
//列举空间文件列表
        BucketManager.FileListIterator fileListIterator = bucketManager.createFileListIterator(bucket, prefix, limit, delimiter);
        while (fileListIterator.hasNext()) {
            //处理获取的file list结果
            FileInfo[] items = fileListIterator.next();
            for (FileInfo item : items) {
                list.add(item.key);
            }
        }
        return list;
    }
}
