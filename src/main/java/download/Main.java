package download;

/**
 * OkHttp 实现多线程下载
 */
public class Main {
    public static void main(String[] args) {
        DownloadManager downloadManager = new DownloadManager("https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk","/Users/dhl/Desktop/PicDemo","temp15.apk");// 8745ms 18530ms
        downloadManager.download();
       // OkHttpManager.getInstance().downloadFile("http://dldir1.qq.com/weixin/android/weixin703android1400.apk","/Users/dhl/Desktop/PicDemo/temp11.apk");//9262ms 59107ms
    }
}
