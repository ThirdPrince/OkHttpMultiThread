package download;

import manager.OkHttpManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DownloadManager {


    private final int THREAD_COUNT = 4;

    private AtomicInteger finishCount = new AtomicInteger(0);

    /**
     * 需要下载资源的地址
     */
    private String url;

    /**
     * 下载的文件
     */
    private File localFile;

    /**
     * 徐亚下载文件存放的本地文件夹路径
     */
    private String dirPath;


    /**
     * 存储到本地的文件名
     */
    private String fileName;

    private long fileSize;


    public DownloadManager(String url,String dir,String fileName){
        this.url = url;
        this.dirPath = dir;
        this.fileName = fileName;
    }

    public void download(){
       getMediaSize();
       long block = fileSize % THREAD_COUNT == 0 ? fileSize / THREAD_COUNT:(fileSize/THREAD_COUNT +1);
       long startTime = System.currentTimeMillis();
       for (int i  = 0;i < THREAD_COUNT;i++){
           long start = i* block;
           long end = start + block >= fileSize?fileSize:start+block-1;
           OkHttpManager.getInstance().downloadFileByRange(url,start,end,new Callback(){

               @Override
               public void onResponse(Call call, Response response) throws IOException {
                   System.out.println(Thread.currentThread().getName());
                   System.out.println(response.code());
                   if(response.code() != 206){
                       return;
                   }
                   InputStream is = response.body().byteStream();
                   int len = 0;
                   byte[] buf = new byte[1024 << 2];
                   RandomAccessFile raf = new RandomAccessFile(localFile,"rwd");
                   raf.seek(start);
                   while ((len = is.read(buf))!= -1){
                       raf.write(buf,0,len);
                   }
                   raf.close();
                   is.close();
                   System.out.println(Thread.currentThread().getId()+":已下载完成");
                   if(finishCount.incrementAndGet() % THREAD_COUNT == 0){
                       long endTime = System.currentTimeMillis();
                       System.out.println((endTime -startTime)+"ms");
                       System.out.println("已全部下载完成");
                   }

               }
               @Override
               public void onFailure(Call call, IOException e) {

               }


           });
       }


    }

    private long getMediaSize(){
        try {
            Response response =  OkHttpManager.getInstance().getContentLength(url);
            if(response.code() != 200){
                throw new IllegalArgumentException("url that you connected has error");
                //return;
            }
            this.fileSize  = response.body().contentLength();
            if(fileSize <= 0){
                throw new IllegalArgumentException(" the file that you download has a wrong size");
            }
            File dir = new File(dirPath);
            if(!dir.exists()){
                dir.mkdirs();
            }
            this.localFile = new File(dir,fileName);
            RandomAccessFile raf = new RandomAccessFile(this.localFile,"rw");
            raf.setLength(fileSize);
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.fileSize;
    }


}
