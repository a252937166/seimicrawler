package com.ouyang.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.BatchStatus;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ouyang on 2017/7/6.
 */
@Component
public class QiniuUtil {

    @Value("${qiniu_ak}")
    private String AK;

    @Value("${qiniu_sk}")
    private String SK;

    @Value("${qiniu_bucket}")
    private String BUCKET;

    @Value("${qiniu_cdn}")
    private String CDN;

    public String uploadImg(MultipartFile file) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone2());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        String key = UUIDUtil.getId() + "_" + file.getOriginalFilename();
        Auth auth = Auth.create(AK, SK);
        String upToken = auth.uploadToken(BUCKET);
        DefaultPutRet putRet = null;
        try {
            Response response = uploadManager.put(file.getBytes(), key, upToken);
            //解析上传成功的结果
            putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return putRet != null ? putRet.key : null;
    }

    public String uploadImg(String fileName, byte[] fileBytes) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone2());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传
        Auth auth = Auth.create(AK, SK);
        String upToken = auth.uploadToken(BUCKET);
        DefaultPutRet putRet = null;
        try {
            Response response = uploadManager.put(fileBytes, fileName, upToken);
            //解析上传成功的结果
            putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        }
        return putRet != null ? putRet.key : null;
    }

    public String uploadImg(String fileName, String imgUrl) {
        byte[] fileBytes = Http.getImageBytes(imgUrl);
        return uploadImg(fileName, fileBytes);
    }

    public String getPrivateImage(String fileName) throws UnsupportedEncodingException {
        if (new Random().nextBoolean()) {
            fileName = fileName + "-info";
        } else {
            fileName = fileName + "-blog";
        }
        String encodedFileName = URLEncoder.encode(fileName, "utf-8");
        String publicUrl = String.format("%s/%s", CDN, encodedFileName);
        Auth auth = Auth.create(AK, SK);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String privateUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        return privateUrl;
    }


    public String getImgUrl(String key) {
        return CDN + "/" + key;
    }

    public List<String> getKeyList() {
        List<String> keyList = new ArrayList<>();
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释
        String accessKey = AK;
        String secretKey = SK;
        String bucket = BUCKET;
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
                keyList.add(item.key);
            }
        }
        return keyList;
    }

    public void deletList(List<String> keyList) {
        //构造一个带指定Zone对象的配置类
        Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释
        String accessKey = AK;
        String secretKey = SK;
        String bucket = BUCKET;
        Auth auth = Auth.create(accessKey, secretKey);
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            //单次批量请求的文件数量不得超过1000
            String[] keyAarry = keyList.toArray(new String[keyList.size()]);
            BucketManager.BatchOperations batchOperations = new BucketManager.BatchOperations();
            batchOperations.addDeleteOp(bucket, keyAarry);
            Response response = bucketManager.batch(batchOperations);
            BatchStatus[] batchStatusList = response.jsonToObject(BatchStatus[].class);
            for (int i = 0; i < keyAarry.length; i++) {
                BatchStatus status = batchStatusList[i];
                String key = keyAarry[i];
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
}
