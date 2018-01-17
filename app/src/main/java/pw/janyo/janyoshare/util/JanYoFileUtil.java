package pw.janyo.janyoshare.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pw.janyo.janyoshare.classes.InstallAPP;
import vip.mystery0.tools.logs.Logs;

public class JanYoFileUtil {
    private static final String TAG = "JanYoFileUtil";
    public static final String USER_LIST_FILE = "user.list";
    public static final String SYSTEM_LIST_FILE = "system.list";

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

    public static boolean saveAppList(Context context, List<InstallAPP> list, String fileName) {
        List<String> stringList = new ArrayList<>();
        for (InstallAPP installAPP : list) {
            stringList.add(installAPP.toString());
        }
        File file = new File(context.getExternalCacheDir(), fileName);
        return saveObject(stringList, file);
    }

    public static boolean saveObject(Object object, File file) {
        Gson gson = new Gson();
        return saveMessage(gson.toJson(object), file);
    }

    public static boolean saveMessage(String message, File file) {
        if (file.exists())
            file.delete();
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

    public static <T> List<T> getListFromFile(File file, Class<T> tClass) {
        if (!file.exists())
            return new ArrayList<>();
        FileInputStream fileInputStream = null;
        try {
            JsonParser parser = new JsonParser();
            Gson gson = new Gson();
            fileInputStream = new FileInputStream(file);
            JsonArray jsonArray = parser.parse(new InputStreamReader(fileInputStream)).getAsJsonArray();
            List<T> list = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray) {
                list.add(gson.fromJson(jsonElement, tClass));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (fileInputStream != null)
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public static boolean isCacheAvailable(Context context) {
        File dir = context.getExternalCacheDir();
        if (dir == null)
            return false;
        int temp = 0;
        for (File file : dir.listFiles()) {
            long now = Calendar.getInstance().getTimeInMillis();
            long modified = file.lastModified();
            if (now - modified >= 3 * 24 * 60 * 60 * 1000) {
                temp++;
                file.deleteOnExit();
            }
        }
        return temp == 0;
    }
}
