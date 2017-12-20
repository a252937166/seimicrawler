package com.ouyang.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import com.ouyang.dao.ComicBasicMapper;
import com.ouyang.model.ComicBasic;
import com.ouyang.service.ComicBasicService;
import com.ouyang.util.HttpUtil;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Crawler(name = "manhua",httpTimeOut = 30000)
public class Manhua extends BaseSeimiCrawler {
    @Autowired
    ComicBasicService comicBasicService;

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
            logger.info("{}", urls.size());
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
        try {
            JXDocument doc = response.document();
            System.out.println(doc.sel("//div[@id='mhimg0']/a/img/@src"));
            //使用神器paoding-jade存储到DB
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void contentBean(Response response) {
        String url = response.getUrl();
        String realUrl = response.getRealUrl();
        Map<String,String> params = HttpUtil.getParams(realUrl);
        try {
            JXDocument doc = response.document();
            System.out.println(doc.sel("//div[@id='mhimg0']/a/img/@src"));
            //使用神器paoding-jade存储到DB
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
