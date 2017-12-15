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
package cn.wanghaomiao.seimi.http.okhttp;

import okhttp3.OkHttpClient;

/**
 * @author 汪浩淼 et.tw@163.com
 * @since 2016/6/26.
 */
public class OkHttpClientBuilderProvider {
    private static class OkHttpclientBuilderProviderHolder {
        public static OkHttpClientBuilderBox okHttpClientBuilderBox = new OkHttpClientBuilderBox();
    }
    public static OkHttpClient.Builder getInstance(){
        return OkHttpclientBuilderProviderHolder.okHttpClientBuilderBox.instance();
    }
}
