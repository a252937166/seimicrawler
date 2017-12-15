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

import com.ouyang.http.SeimiCookie;
import com.ouyang.struct.Request;
import com.ouyang.struct.Response;

import java.util.List;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/6/26.
 */
public interface SeimiDownloader {
    /**
     * 处理抓取请求生成response
     */
    Response process(Request request) throws Exception;

    /**
     * 处理meta标签refresh场景
     *
     * @param nextUrl 重定向URL
     * @return 请求的最终返回体
     */
    Response metaRefresh(String nextUrl) throws Exception;

    /**
     * http请求状态
     * @return http状态码
     */
    int statusCode();

    /**
     * 添加自定义cookies
     * @param url 目标地址
     * @param seimiCookies cookies
     */
    void addCookies(String url, List<SeimiCookie> seimiCookies);

}
