package com.example.alex.wifitalkdemo;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by baochaoh on 2017/7/8.
 */

public class FileUtil {

    File file = new File(Environment.getExternalStorageDirectory(),
            "test");

    /*读取一段数据 并保存到指定路径中*/
    public void writeVideoData(byte[] data){
        try {
            FileOutputStream fos = new FileOutputStream(file,true);
            fos.write(data);
            String str = "################\n";
            fos.write(str.getBytes());
            fos.close();
            System.out.println("写入成功：data");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
