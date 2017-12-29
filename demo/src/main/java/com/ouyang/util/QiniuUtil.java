package com.ouyang.util;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        String key = UUIDUtil.getId()+"_"+file.getOriginalFilename();
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
        return putRet!=null?putRet.key:null;
    }

    public String uploadImg(String fileName,byte[] fileBytes) {
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
        return putRet!=null?putRet.key:null;
    }

    public String uploadImg(String fileName,String imgUrl) {
        byte[] fileBytes = Http.getImageBytes(imgUrl);
        return uploadImg(fileName,fileBytes);
    }

    public String getPrivateImage(String fileName) throws UnsupportedEncodingException {
        if (new Random().nextBoolean()) {
            fileName = fileName+"-info";
        } else {
            fileName = fileName+"-blog";
        }
        String encodedFileName = URLEncoder.encode(fileName, "utf-8");
        String publicUrl = String.format("%s/%s", CDN, encodedFileName);
        Auth auth = Auth.create(AK, SK);
        long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
        String privateUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        return privateUrl;
    }


    public String getImgUrl(String key) {
        return CDN+"/"+key;
    }
}
