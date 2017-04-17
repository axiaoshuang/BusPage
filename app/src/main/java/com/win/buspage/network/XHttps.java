package com.win.buspage.network;


import com.win.buspage.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Author: wangshuang
 * Time: 2017/1/6
 * Email:xiaoshuang990@sina.com
 */

public class XHttps extends OkHttpClientManager{

        public static XHttps mInstance;

        private XHttps() {
            super();
        }
        public static XHttps getInstance()
        {
            if (mInstance == null)
            {
                synchronized (XHttps.class)
                {
                    if (mInstance == null)
                    {
                        mInstance = new XHttps();
                    }
                }
            }
            return mInstance;
        }

        /**
         * 异步的post请求
         *
         * @param json
         * @param callback
         */
        private <T> void _postAsyn(String json, final ResultCallback<T> callback)
        {
            final Request request = new Request.Builder()
                    .url(Config.BASE_URL)
                    .post(getRequestBody(json))
                    .build();
            deliveryResult(callback, request);
        }

        /**
         * 异步基于post的文件上传
         *
         * @param json
         * @param callback
         * @param files
         * @param fileKeys
         * @throws IOException
         */
        private <T> void _postAsyn(String json, ResultCallback<T> callback, File[] files, String[] fileKeys) throws IOException
        {
            Request request = buildMultipartFormRequest( files, fileKeys, json);
            deliveryResult(callback, request);

        }

        //异步
        public static <T> void postAsyn(String json, ResultCallback<T> callback)
        {
            getInstance()._postAsyn(json,callback);
        }
        //异步
        public static <T> void postAsyn(String json, ResultCallback<T> callback, File[] files, String[] fileKeys) throws IOException
        {
            getInstance()._postAsyn(json, callback, files, fileKeys);
        }

        protected RequestBody getRequestBody(String json){
            return RequestBody.create(null,json);
        }

        private Request buildMultipartFormRequest(File[] files,
                                                  String[] fileKeys, String json)
        {
            MultipartBody.Builder builde = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            builde.addPart(Headers.of("Content-Disposition", "form-data; name=\"json\""),
                    RequestBody.create(null, json));

            if (files != null)
            {
                RequestBody fileBody = null;
                for (int i = 0; i < files.length; i++)
                {
                    File file = files[i];
                    String fileName = file.getName();
                    fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                    //TODO 根据文件名设置contentType
                    builde.addPart(Headers.of("Content-Disposition",
                            "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                            fileBody);
                }
            }
            MultipartBody builder = builde.build();
            Request.Builder post = new Request.Builder()
                    .url(Config.BASE_URL)
                    .post(builder);
            if (getHeaders()!=null)
                post.headers(getHeaders());
            return post
                    .build();
        }


    public static String getUrlString(String programName, Map<String,String> map) {
        if(map!=null){
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            return Config.BASE_URL2+programName+"?"+ sb.toString();
        }
        return "";
    }

    public static String getLiftString(String programName, Map<String,String> map) {
        if(map!=null){
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            return Config.BASE_URL3+programName+"?"+ sb.toString();
        }
        return "";
    }

    public static String getRequsetString(String programName, Map<String,String> map) {
        if(map!=null){
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            return Config.BASE_URL+programName+"?"+ sb.toString();
        }
        return "";
    }

    public static String DustNoiseImage = "http://120.27.193.52:8089/Photo/";

}
