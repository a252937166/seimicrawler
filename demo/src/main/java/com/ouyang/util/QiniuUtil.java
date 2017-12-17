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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Created by Ouyang on 2017/7/6.
 */
public class QiniuUtil {

    @Value("${qiniu_ak}")
    private static String AK;

    @Value("${qiniu_sk}")
    private static String SK;

    @Value("${qiniu_bucket}")
    private static String BUCKET;

    @Value("${qiniu_cdn}")
    private static String CDN;

    public static String uploadImg(MultipartFile file) {
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

    public static String uploadImg(String fileName,byte[] fileBytes) {
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

    public static String uploadImg(String fileName,String imgUrl) {
        byte[] fileBytes = Http.getImageBytes(imgUrl);
        return uploadImg(fileName,fileBytes);
    }


    public static String getImgUrl(String key) {
        return CDN+"/"+key;
    }
}
