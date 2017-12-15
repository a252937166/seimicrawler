/*
   Copyright 2015 Wang Haomiao<et.tw@163.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ouyang.core;


import com.ouyang.annotation.Crawler;
import com.ouyang.annotation.Interceptor;
import com.ouyang.annotation.Queue;
import com.ouyang.def.BaseSeimiCrawler;
import com.ouyang.exception.SeimiInitExcepiton;
import com.ouyang.struct.CrawlerModel;
import com.ouyang.utils.StrFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * 初始化上下文环境
 * @author 汪浩淼 [et.tw@163.com]
 *         Date: 2015/6/26.
 */
public class SeimiContext  extends AnnotationConfigApplicationContext {
    private int BASE_THREAD_NUM = 2;
    protected ApplicationContext applicationContext;
    protected SeimiScanner seimiScanner;
    protected Set<Class<? extends BaseSeimiCrawler>> crawlers;
    protected Set<Class<? extends SeimiQueue>> hasUsedQuene;
    protected List<SeimiInterceptor> interceptors;
    protected Map<String,CrawlerModel> crawlerModelContext;
    protected ExecutorService workersPool;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    public SeimiContext(){
        register(ScanConfig.class);
        init();
        if(!CollectionUtils.isEmpty(crawlers)){
            prepareCrawlerModels();
            workersPool = Executors.newFixedThreadPool(BASE_THREAD_NUM*Runtime.getRuntime().availableProcessors()*crawlers.size());
            prepareWorkerThread();
        }else {
            logger.error("can not find any crawlers,please check!");
        }
    }

    protected void waitToEnd(){
        workersPool.shutdown();
        try {
            workersPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
    }

    private void init(){
        String[] targetPkgs = {"crawlers","queues","interceptors","com.ouyang"};
        seimiScanner = new SeimiScanner(this);
        Set<Class<?>> aladdin = seimiScanner.scan(targetPkgs, Crawler.class, Queue.class, Interceptor.class);
        applicationContext = this;
        crawlers = new HashSet<>();
        hasUsedQuene = new HashSet<>();
        interceptors = new LinkedList<>();
        crawlerModelContext = new HashMap<>();
        List<Class<?>> registList = new LinkedList<>();
        for (Class cls:aladdin){
            if (BaseSeimiCrawler.class.isAssignableFrom(cls)){
                Crawler c = (Crawler) cls.getAnnotation(Crawler.class);
                hasUsedQuene.add(c.queue());
                crawlers.add(cls);
                registList.add(cls);
            }else if (SeimiInterceptor.class.isAssignableFrom(cls)){
                registList.add(cls);
            }
        }
        //收集会使用到的seimiQueue并注册到context
        for (Class cls:aladdin){
            if (SeimiQueue.class.isAssignableFrom(cls)&&hasUsedQuene.contains(cls)){
                registList.add(cls);
            }
        }
        //统一注册需要用到的类
        seimiScanner.regist(registList);
        //获取注册后的拦截器实例
        for (Class cls:aladdin){
            if (SeimiInterceptor.class.isAssignableFrom(cls)){
                interceptors.add((SeimiInterceptor)applicationContext.getBean(cls));
            }
        }
        Collections.sort(interceptors, new Comparator<SeimiInterceptor>() {
            //对拦截器按照设定的权重进行倒序排序，如：88,66,11
            @Override
            public int compare(SeimiInterceptor o1, SeimiInterceptor o2) {
                return o1.getWeight() > o2.getWeight() ? -1 : 1;
            }
        });
    }

    private void prepareCrawlerModels(){
        for (Class<? extends BaseSeimiCrawler> a:crawlers){
            CrawlerModel crawlerModel = new CrawlerModel(a,applicationContext);
            if (crawlerModelContext.containsKey(crawlerModel.getCrawlerName())){
                logger.error("Crawler:{} is repeated,please check",crawlerModel.getCrawlerName());
                throw new SeimiInitExcepiton(StrFormatUtil.info("Crawler:{} is repeated,please check",crawlerModel.getCrawlerName()));
            }
            crawlerModelContext.put(crawlerModel.getCrawlerName(),crawlerModel);
        }
    }

    private void prepareWorkerThread(){
        for (Map.Entry<String,CrawlerModel> crawlerEntry:crawlerModelContext.entrySet()){
            for (int i =0;i<BASE_THREAD_NUM*Runtime.getRuntime().availableProcessors();i++){
                workersPool.execute(new SeimiProcessor(interceptors,crawlerEntry.getValue()));
            }
        }
    }

}
