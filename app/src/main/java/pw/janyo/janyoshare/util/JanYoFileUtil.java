package pw.janyo.janyoshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import vip.mystery0.tools.logs.Logs;

public class JanYoFileUtil {
    private static final String TAG = "JanYoFileUtil";

    public static boolean copyFile(String inputPath, String outputPath) {
        if (!(new File(inputPath)).exists()) {
            Logs.e(TAG, "copyFile: 输入文件不存在");
            return false;
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(inputPath);
            fileOutputStream = new FileOutputStream(outputPath);
            byte[] bytes = new byte[1024 * 1024 * 10];
            int readCount = 0;
            while (readCount != -1) {
                fileOutputStream.write(bytes, 0, readCount);
                readCount = fileInputStream.read(bytes);
            }
            return true;
        } catch (Exception e) {
            Logs.wtf(TAG, "copyFile: ", e);
            return false;
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
