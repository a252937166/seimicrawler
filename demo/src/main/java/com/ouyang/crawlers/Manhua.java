package com.ouyang.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

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
            List<Object> urls = doc.sel("//div[@id='mhnew']/li/a/@href");
            logger.info("{}", urls.size());
            for (Object s : urls) {
                push(Request.build("http://"+s.toString().split("//")[1], "renderBean")
                        .useSeimiAgent()
                        .setSeimiAgentRenderTime(5000));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderBean(Response response) {
        try {
            JXDocument doc = response.document();
            System.out.println(doc.sel("//div[@id='mhimg0']/a/img/@src"));
            //使用神器paoding-jade存储到DB
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
