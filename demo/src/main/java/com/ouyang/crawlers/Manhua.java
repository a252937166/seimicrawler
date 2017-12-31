package com.ouyang.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import com.ouyang.model.ComicChapter;
import com.ouyang.model.ComicContent;
import com.ouyang.model.ComicErrorContent;
import com.ouyang.service.ComicBasicService;
import com.ouyang.service.ComicChapterService;
import com.ouyang.service.ComicContentService;
import com.ouyang.service.ComicErrorContentService;
import com.ouyang.util.Http;
import com.ouyang.util.HttpUtil;
import com.ouyang.util.QiniuUtil;
import com.ouyang.util.UUIDUtil;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Crawler(name = "manhua", httpTimeOut = 30000)
public class Manhua extends BaseSeimiCrawler {
    @Autowired
    ComicBasicService comicBasicService;
    @Autowired
    ComicChapterService comicChapterService;
    @Autowired
    ComicContentService comicContentService;
    @Autowired
    QiniuUtil qiniuUtil;
    @Autowired
    ComicErrorContentService comicErrorContentService;

    @Value("${seimiAgentHost}")
    private String seimiAgentHost;

    @Value("${seimiAgentPort}")
    private int seimiAgentPort;

    @Override
    public String seimiAgentHost() {
        return this.seimiAgentHost;
    }

    @Override
    public int seimiAgentPort() {
        return this.seimiAgentPort;
    }

    @Override
    public String[] startUrls() {
        return new String[]{"http://www.fzdm.com/"};
    }


    @Override
    public void start(Response response) {
//        //所有热门
//        JXDocument doc = response.document();
//        try {
//            List<Object> urls = doc.sel("//div[@id='box1']/li/a");
//            logger.info("start size :: {}", urls.size());
//            for (Object s : urls) {
//                Element e = (Element) s;
//                String comicName = e.childNode(0).toString();
//                String basicId = comicBasicService.getIdByName(comicName);
//                String chapterUrl = "http://"+e.attr("href").split("//")[1];
//                Map<String, String> params = new HashMap<>();
//                params.put("basicId",basicId);
//                params.put("comicName",comicName);
//                push(Request.build(chapterUrl, "chapterBean").setParams(params));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        //单章节
//        Map<String, String> param = new HashMap<>();
//        String contentUrl = "http://manhua.fzdm.com/1/Vol_002";
//        param.put("chapterName", HttpUtil.paramEncode("火影忍者第2卷"));
//        param.put("comicName", HttpUtil.paramEncode("火影忍者"));
//        push(Request.build(contentUrl, "contentBean")
//                .setMeta(param)
//                .useSeimiAgent()
//                .setSeimiAgentUseCookie(true)
//                .setSeimiAgentRenderTime(5000)
//        );

        //单本漫画
        try {
            String comicName ="一拳超人";
            String basicId = comicBasicService.getIdByName(comicName);
            String chapterUrl = "http://manhua.fzdm.com/132/";
            Map<String, String> params = new HashMap<>();
            params.put("basicId", basicId);
            params.put("comicName", comicName);
            push(Request.build(chapterUrl, "chapterBean").setParams(params));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void chapterBean(Response response) {
        String url = response.getUrl();
        Map params = response.getRequest().getParams();
        String basicId = (String) params.get("basicId");
        String comicName = (String) params.get("comicName");
        comicName = HttpUtil.paramDecode(comicName);
        Pattern chapterNumberPattern = Pattern.compile("^" + comicName + "\\s*(\\d+)话");
        Pattern chapterNamePattern = Pattern.compile("\\[(.+)]");
        try {
            JXDocument doc = response.document();
            List<Object> urls = doc.sel("//div[@id='content']/li/a");
            for (int i = urls.size() - 1; i >= 0; i--) {
                Element e = (Element) urls.get(i);
                String eName = e.childNode(0).toString();
                ComicChapter comicChapter = new ComicChapter();
                comicChapter.setBasicId(basicId);
                comicChapter.setOriginName(eName);
                if (comicChapterService.select(comicChapter) == null) {
                    Matcher chapterNumberMatcher = chapterNumberPattern.matcher(eName);
                    if (chapterNumberMatcher.find()) {
                        comicChapter.setChapterNo(Integer.valueOf(chapterNumberMatcher.group(1)));
                    }
                    Matcher chapterNameMatcher = chapterNamePattern.matcher(eName);
                    if (chapterNameMatcher.find()) {
                        comicChapter.setName(chapterNameMatcher.group(1));
                    }
                    comicChapter.setCreateDate(new Date());
                    comicChapter.setId(UUIDUtil.getId());
                    comicChapterService.insert(comicChapter);
                }
                String contentUrl = url + e.attr("href");
                Map<String, String> param = new HashMap<>();
                param.put("chapterId", comicChapter.getId());
                param.put("chapterName", HttpUtil.paramEncode(eName));
                param.put("comicName", HttpUtil.paramEncode(comicName));
                if (contentUrl.endsWith("/")) {
                    contentUrl = contentUrl.substring(0, contentUrl.length() - 1);
                }
                push(Request.build(contentUrl, "contentBean")
                        .setMeta(param)
                        .useSeimiAgent()
                        .setSeimiAgentRenderTime(6000)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void contentBean(Response response) {
        Map<String, String> params = response.getRequest().getMeta();
        String chapterId = params.get("chapterId");
        String chapterName = HttpUtil.paramDecode(params.get("chapterName"));
        String comicName = HttpUtil.paramDecode(params.get("comicName"));
        try {
            JXDocument doc = response.document();
            List<Object> imgUrlList = doc.sel("//img[@id='mhpic']/@src");
            if (CollectionUtils.isEmpty(imgUrlList)) {
                ComicErrorContent comicErrorContent = new ComicErrorContent();
                comicErrorContent.setChapterId(chapterId);
                comicErrorContent.setCreateDate(new Date());
                comicErrorContent.setImgUrl(response.getUrl());
                comicErrorContentService.insert(comicErrorContent);
                return;
            }
            String imgUrl = (String) imgUrlList.get(0);
            List<Object> elements = doc.sel("//a[@id='mhona']");
            Map<String, String> mhonaMap = new HashMap<>();
            ComicContent comicContent = new ComicContent();
            for (Object e : elements) {
                Element element = (Element) e;
                String text = element.childNode(0).toString();
                mhonaMap.put(text, element.attr("href"));
                Pattern pageNumberPattern = Pattern.compile("第(\\d+)页");
                Matcher pageNumberMatcher = pageNumberPattern.matcher(text);
                if (pageNumberMatcher.find()) {
                    comicContent.setPageNo(Integer.valueOf(pageNumberMatcher.group(1)));
                }
            }
            comicContent.setChapterId(chapterId);
            comicContent.setCreateDate(new Date());
            byte[] fileBytes = Http.getImageBytes(imgUrl);
            String imgType = "jpg";
            try {
                imgType = Http.getImgeType(fileBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String fileName = comicName + "/" + chapterName + "/" + comicContent.getPageNo() + "." + imgType;
            comicContent.setImgUrl(qiniuUtil.getPrivateImage(qiniuUtil.uploadImg(fileName, fileBytes)));
            comicContent.setFileName(fileName);
            comicContent.setUpdateDate(new Date());
            comicContentService.insert(comicContent);
            System.out.println(fileName + "::" + comicContent.getImgUrl());
            if (mhonaMap.containsKey("下一页")) {
                Map<String, String> param = new HashMap<>();
                String currentUrl = response.getUrl();
                if (currentUrl.endsWith("html")) {
                    currentUrl = currentUrl.substring(0, currentUrl.lastIndexOf("/"));
                }
                String contentUrl = currentUrl + "/" + mhonaMap.get("下一页");
                param.put("chapterName", HttpUtil.paramEncode(chapterName));
                param.put("comicName", HttpUtil.paramEncode(comicName));
                push(Request.build(contentUrl, "contentBean")
                        .setMeta(param)
                        .useSeimiAgent()
                        .setSeimiAgentRenderTime(6000)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
