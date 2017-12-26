package com.ouyang.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import com.ouyang.dao.ComicBasicMapper;
import com.ouyang.dao.ComicChapterMapper;
import com.ouyang.model.ComicBasic;
import com.ouyang.model.ComicChapter;
import com.ouyang.model.ComicContent;
import com.ouyang.service.ComicBasicService;
import com.ouyang.service.ComicChapterService;
import com.ouyang.util.HttpUtil;
import com.ouyang.util.UUIDUtil;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Crawler(name = "manhua",httpTimeOut = 30000)
public class Manhua extends BaseSeimiCrawler {
    @Autowired
    ComicBasicService comicBasicService;
    @Autowired
    ComicChapterService comicChapterService;

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
        return new String []{"http://www.fzdm.com/"};
    }


    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//div[@id='box1']/li/a");
            logger.info("start size :: {}", urls.size());
            for (Object s : urls) {
                Element e = (Element) s;
                String comicName = e.childNode(0).toString();
                String basicId = comicBasicService.getIdByName(comicName);
                String chapterUrl = "http://"+e.attr("href").split("//")[1];
                Map<String, String> params = new HashMap<>();
                params.put("basicId",basicId);
                params.put("comicName",comicName);
                push(Request.build(chapterUrl, "chapterBean").setParams(params));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chapterBean(Response response) {
        String url = response.getUrl();
        String realUrl = response.getRealUrl();
        String basicId = HttpUtil.getParams(realUrl).get("basicId");
        String comicName = HttpUtil.getParams(realUrl).get("comicName");
        comicName = HttpUtil.paramDecode(comicName);
        Pattern chapterNumberPattern = Pattern.compile("^"+comicName+"(\\d+)话");
        Pattern chapterNamePattern = Pattern.compile("\\[(.+)\\]");
        try {
            JXDocument doc = response.document();
            List<Object> urls = doc.sel("//div[@id='content']/li/a");
            for (int i = urls.size()-1 ; i >=0 ;i-- ) {
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
                String contentUrl = url+e.attr("href");
                Map<String, String> params = new HashMap<>();
                params.put("chapterId",comicChapter.getId());
                push(Request.build(contentUrl, "contentBean")
                        .useSeimiAgent()
                        .setSeimiAgentUseCookie(true)
                        .setSeimiAgentRenderTime(6000));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void contentBean(Response response) {
        String url = response.getUrl();
        String realUrl = response.getRealUrl();
        Map<String,String> params = HttpUtil.getParams(realUrl);
        String chapterId = params.get("chapterId");
        try {
            JXDocument doc = response.document();
            String imgUrl = (String) doc.sel("//div[@id='mhimg0']/a/img/@src").get(0);
            String pageStr = (String) doc.sel("//a[@id='mhona']/text()").get(0);
            Pattern pageNumberPattern = Pattern.compile("第(\\d+)页");
            Matcher pageNumberMatcher = pageNumberPattern.matcher(pageStr);
            ComicContent comicContent = new ComicContent();
            comicContent.setChapterId(chapterId);
            if (pageNumberMatcher.find()) {
                comicContent.setPageNo(Integer.valueOf(pageNumberMatcher.group(1)));
            }
            System.out.println(imgUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
