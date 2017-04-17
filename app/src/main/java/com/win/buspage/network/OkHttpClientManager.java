package com.win.buspage.network;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.win.buspage.base.BaseApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientManager {
    private static OkHttpClientManager mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private Gson mGson;
    private PutHeader putHeader;
    public static PutHeader putHeaders;

    private static final String TAG = "OkHttp";

    protected OkHttpClientManager() {
        mOkHttpClient = new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor(
                new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        Log.d(TAG, "System out:"+message);
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY)).build();

        mDelivery = new Handler(Looper.getMainLooper());
        mGson = BaseApplication.getGson();
    }

    public static OkHttpClientManager getInstance() {
        if (mInstance == null) {
            synchronized (OkHttpClientManager.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpClientManager();
                }
            }
        }
        return mInstance;
    }

    public static OkHttpClientManager getInstance(PutHeader putHeader) {
        getInstance();
        mInstance.setPutHeader(putHeader);
        return mInstance;
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return Response
     */
    private Response _getSyn(String url) throws IOException {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        return call.execute();
    }

    /**
     * 同步的Get请求
     *
     * @param url
     * @return 字符串
     */
    private String _getString(String url) throws IOException {
        Response execute = _getSyn(url);
        return execute.body().string();
    }


    /**
     * 异步的get请求
     *
     * @param url
     * @param callback
     */
    private <T> void _getAsyn(String url, final ResultCallback<T> callback) {
        final Request request = new Request.Builder()
                .url(url)
                .build();
        deliveryResult(callback, request);
    }


    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return
     */
    private Response _post(String url, Param... params) throws IOException {
        Request request = buildPostRequest(url, params);
        return mOkHttpClient.newCall(request).execute();
    }


    /**
     * 同步的Post请求
     *
     * @param url
     * @param params post的参数
     * @return 字符串
     */
    private String _postString(String url, Param... params) throws IOException {
        Response response = _post(url, params);
        return response.body().string();
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private <T> void _postAsyn(String url, final ResultCallback<T> callback, Param... params) {
        Request request = buildPostRequest(url, params);
        deliveryResult(callback, request);
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    private <T> void _postAsyn(String url, final ResultCallback<T> callback, Map<String, String> params) {
        Param[] paramsArr = map2Params(params);
        Request request = buildPostRequest(url, paramsArr);
        deliveryResult(callback, request);
    }

    /**
     * 同步基于post的文件上传
     *
     * @param params
     * @return
     */
    private Response _post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        return mOkHttpClient.newCall(request).execute();
    }

    private Response _post(String url, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        return mOkHttpClient.newCall(request).execute();
    }

    private Response _post(String url, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        return mOkHttpClient.newCall(request).execute();
    }

    /**
     * 异步基于post的文件上传
     *
     * @param url
     * @param callback
     * @param files
     * @param fileKeys
     * @throws IOException
     */
    private <T> void _postAsyn(String url, ResultCallback<T> callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, files, fileKeys, params);
        deliveryResult(callback, request);
    }

    /**
     * 异步基于post的文件上传，单文件不带参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @throws IOException
     */
    private <T> void _postAsyn(String url, ResultCallback<T> callback, File file, String fileKey) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, null);
        deliveryResult(callback, request);
    }

    /**
     * 异步基于post的文件上传，单文件且携带其他form参数上传
     *
     * @param url
     * @param callback
     * @param file
     * @param fileKey
     * @param params
     * @throws IOException
     */
    private <T> void _postAsyn(String url, ResultCallback<T> callback, File file, String fileKey, Param... params) throws IOException {
        Request request = buildMultipartFormRequest(url, new File[]{file}, new String[]{fileKey}, params);
        deliveryResult(callback, request);
    }

    /**
     * 异步下载文件
     *
     * @param url
     * @param destFileDir 本地文件存储的文件夹
     * @param callback
     * @param listener  下载进度监听
     */
    private void _downloadAsyn(final String url, final String destFileDir, final ResultCallback<String> callback, final OnDownloadListener listener) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        if (headers != null)
            builder.headers(headers);
        if (putHeader != null)
            putHeader.putHeaders(builder);
        final Request request = builder
                .build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(destFileDir, getFileName(url));
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    //如果下载文件成功，第一个参数为文件的绝对路径
                    sendSuccessResultCallback(file.getAbsolutePath(), callback);
                } catch (IOException e) {
                    sendFailedStringCallback(e, callback);
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException ignored) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    private String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }


    private <T> void setErrorResId(final ImageView view, final int errorResId) {
        mDelivery.post(new Runnable()
        {
            @Override
            public void run()
            {
                view.setImageResource(errorResId);
            }
        });
    }


    //*************对外公布的方法************

    //同步
    public static Response getAsyn(String url) throws IOException {
        return getInstance()._getSyn(url);
    }

    //同步
    public static String getAsString(String url) throws IOException {
        return getInstance()._getString(url);
    }

    //异步
    public static <T> void getAsyn(String url, ResultCallback<T> callback) {
        getInstance()._getAsyn(url, callback);
    }

    //同步
    public static Response post(String url, Param... params) throws IOException {
        return getInstance()._post(url, params);
    }

    //同步
    public static String postString(String url, Param... params) throws IOException {
        return getInstance()._postString(url, params);
    }

    //异步
    public static <T> void postAsyn(String url, final ResultCallback<T> callback, Param... params) {
        getInstance()._postAsyn(url, callback, params);
    }

    //异步
    public static <T> void postAsyn(String url, final ResultCallback<T> callback, Map<String, String> params) {
        getInstance()._postAsyn(url, callback, params);
    }

    //同步
    public static Response post(String url, File[] files, String[] fileKeys, Param... params) throws IOException {
        return getInstance()._post(url, files, fileKeys, params);
    }

    //同步
    public static Response post(String url, File file, String fileKey) throws IOException {
        return getInstance()._post(url, file, fileKey);
    }

    //同步
    public static Response post(String url, File file, String fileKey, Param... params) throws IOException {
        return getInstance()._post(url, file, fileKey, params);
    }

    //异步
    public static <T> void postAsyn(String url, ResultCallback<T> callback, File[] files, String[] fileKeys, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, files, fileKeys, params);
    }

    //异步
    public static <T> void postAsyn(String url, ResultCallback<T> callback, File file, String fileKey) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey);
    }

    //异步
    public static <T> void postAsyn(String url, ResultCallback<T> callback, File file, String fileKey, Param... params) throws IOException {
        getInstance()._postAsyn(url, callback, file, fileKey, params);
    }

//    public static <T> void displayImage(final ImageView view, String url, int errorResId) throws IOException {
//        getInstance()._displayImage(view, url, errorResId);
//    }
//
//
//    public static <T> void displayImage(final ImageView view, String url) {
//        getInstance()._displayImage(view, url, -1);
//    }

    //异步
    public static void downloadAsyn(String url, String destDir, ResultCallback<String> callback, OnDownloadListener listener) {
        getInstance()._downloadAsyn(url, destDir, callback,listener);
    }

    /**
     *
     * @param path  下载文件的 url
     * @param destDir  输出的目录
     * @param listener  下载的监听
     */
    public static void _downloadAsyn2(String path, String destDir, OnDownloadListener listener){
        try {
            boolean equals = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
            if (equals) {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
    //            //获取到文件的大小
    //            pd.setMax(conn.getContentLength());
                int total = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File file = new File(destDir, getInstance().getFileName(path));
                if(file.exists()){
                    file.delete();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                int len;
                int sum = 0;
                while ((len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    //获取当前下载量
                    listener.onDownloading(progress);
    //                pd.setProgress(total);
                }
                fos.close();
                bis.close();
                is.close();
                listener.onDownloadSuccess();
            }
        } catch (IOException e) {
            e.printStackTrace();
            listener.onDownloadFailed();

        }
    }

    //****************************


    private Request buildMultipartFormRequest(String url, File[] files,
                                              String[] fileKeys, Param[] params) {
        params = validateParam(params);
        MultipartBody.Builder builds = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Param param : params) {
            builds.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + param.key + "\""),
                    RequestBody.create(null, param.value));
        }

        if (files != null) {
            RequestBody fileBody;
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                //TODO 根据文件名设置contentType
                builds.addPart(Headers.of("Content-Disposition",
                        "form-data; name=\"" + fileKeys[i] + "\"; filename=\"" + fileName + "\""),
                        fileBody);
            }
        }
        MultipartBody builder = builds.build();
        Request.Builder post = new Request.Builder()
                .url(url)
                .post(builder);
        if (headers != null)
            post.headers(headers);
        if (putHeader != null)
            putHeader.putHeaders(post);
        return post
                .build();
    }

    protected String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }


    private Param[] validateParam(Param[] params) {
        if (params == null)
            return new Param[0];
        else return params;
    }

    private Param[] map2Params(Map<String, String> params) {
        if (params == null) return new Param[0];
        int size = params.size();
        Param[] res = new Param[size];
        Set<Map.Entry<String, String>> entries = params.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            res[i++] = new Param(entry.getKey(), entry.getValue());
        }
        return res;
    }

    private static final String SESSION_KEY = "Set-Cookie";
    private static final String mSessionKey = "JSESSIONID";

    private Map<String, String> mSessions = new HashMap<String, String>();

    public <T> void deliveryResult(final ResultCallback<T> callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailedStringCallback(e, callback);
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                try {
                    final String string = response.body().string();
                    if (callback.mType == String.class) {
                        sendSuccessResultCallback((T) string, callback);
                    } else {
//                        Class<? extends ResultCallback> aClass = callback.getClass();//内部类
//                        Type mType = callback.mType;
//                        LogUtil.e(string + aClass + TypeToken.get(mType).getType());
//                        TypeToken<?> typeToken = TypeToken.get(callback.mType);
//                        Type type = typeToken.getType();
                        T o = mGson.fromJson(string,callback.mType);
                        sendSuccessResultCallback(o, callback);
                    }


                } catch (IOException | com.google.gson.JsonParseException e) {
                    sendFailedStringCallback(e, callback);
                }

            }
        });
    }

    private <T> void sendFailedStringCallback(final Exception e, final ResultCallback<T> callback) {

        mDelivery.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (callback != null)
                    callback.onError(e);
            }
        });
    }

    private <T> void sendSuccessResultCallback(final T object, final ResultCallback<T> callback) {
        mDelivery.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (callback != null)
                {
                    callback.onResponse(object);
                }
            }
        });
    }

    private Request buildPostRequest(String url, Param[] params) {
        if (params == null) {
            params = new Param[0];
        }
        FormBody.Builder builder = new FormBody.Builder();

        for (Param param : params) {
            builder.add(param.key, param.value);
        }
        RequestBody requestBody = builder.build();
        Request.Builder post = new Request.Builder()
                .url(url)
                .post(requestBody);
        if (headers != null)
            post.headers(headers);
        if (putHeader != null)
            putHeader.putHeaders(post);
        return post
                .build();
    }


    public static abstract class ResultCallback<T> {
        public Type mType;

        public ResultCallback() {
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterize = (ParameterizedType) superclass;
            return $Gson$Types.canonicalize(parameterize.getActualTypeArguments()[0]);
        }

        public abstract void onError(Exception e);

        public abstract void onResponse(T response);
    }

    public static class Param {
        public Param() {
        }

        public Param(String key, String value) {
            this.key = key;
            this.value = value;
        }

        String key;
        String value;
    }

    public PutHeader getPutHeader() {
        return putHeader;
    }

    public void setPutHeader(PutHeader putHeader) {
        this.putHeader = putHeader;
    }

    public static <T> void addPublicHeader(Headers headers) {
        getInstance().setHeaders(headers);
    }

    public void cleanHeader() {
        this.putHeader = null;
    }

    Headers headers;

    public Headers getHeaders() {
        return headers;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    interface PutHeader {
        void putHeaders(Request.Builder builde);
    }
    public static interface OnDownloadListener{
        public void onDownloadSuccess();
        public void onDownloadFailed();
        public void onDownloading(int progress);

    }
}
