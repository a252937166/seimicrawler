package com.ouyang.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import com.ouyang.util.HttpUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;

import javax.naming.ldap.PagedResultsControl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Crawler(name = "manhua",httpTimeOut = 30000)
public class Manhua extends BaseSeimiCrawler {

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
                String basicId = "basicId_____";
                String url = "http://"+e.attr("href").split("//")[1];
                Map map = new HashMap();
                map.put("basicId",basicId);
                push(Request.build(url, "renderBean").setParams(map));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderBean(Response response) {
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
