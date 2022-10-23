package manager;

import okhttp3.*;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * 封装Okhttp
 */
public class OkHttpManager {

    private static volatile OkHttpManager instance;

    private OkHttpClient okHttpClient;

    /**
     * 超时时间
     */
    private final static long CONNECT_TIMEOUT = 60;

    /**
     * 读取时间
     */
    private final static long READ_TIMEOUT = 60;


    /**
     * 写入时间
     */
    private final static long WRITE_TIMEOUT = 60;


    private OkHttpManager() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public static OkHttpManager getInstance() {
        if (instance == null) {
            synchronized (OkHttpManager.class) {
                if (instance == null) {
                    instance = new OkHttpManager();
                }
            }
        }
        return instance;
    }

    public void downloadFileByRange(String url, long startIndex, long endIndex, Callback callback) {
        Request request = new Request.Builder().header("RANGE", "bytes=" + startIndex + "-" + endIndex)
                .url(url)
                .build();
        doAsync(request, callback);
    }

    private Response doSync(Request request) throws IOException {
        Call call = okHttpClient.newCall(request);
        return call.execute();
    }

    private void doAsync(Request request, Callback callback) {
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public Response getContentLength(String url) throws IOException {
        Request request = new Request.Builder().url(url)
                .build();
        return doSync(request);
    }

    public void get(String url, Callback callback) {
        Request request = new Request.Builder().url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void downloadFile(String url,String fileName) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = okHttpClient.newCall(request);
        long startTime = System.currentTimeMillis();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    InputStream is = response.body().byteStream();
                    int len = 0;
                    byte[] buf = new byte[1024 << 2];
                    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                    while ((len = is.read(buf)) != -1) {
                        fileOutputStream.write(buf, 0, len);
                    }
                    fileOutputStream.close();
                    is.close();
                    long endTime = System.currentTimeMillis();
                    System.out.println((endTime - startTime)+"ms");
                    System.out.println(Thread.currentThread().getId() + ":已下载完成");

                }

            }
        });
    }
}



