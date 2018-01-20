package pw.janyo.janyoshare.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pw.janyo.janyoshare.APP;
import pw.janyo.janyoshare.R;
import pw.janyo.janyoshare.classes.InstallAPP;
import vip.mystery0.tools.logs.Logs;

public class JanYoFileUtil {
    private static final String TAG = "JanYoFileUtil";
    private static final String JANYO_SHARE = "JanYo Share";//临时目录名
    public static final String USER_LIST_FILE = "user.list";//用户软件列表存储文件名
    public static final String SYSTEM_LIST_FILE = "system.list";//系统软件列表存储文件名

    public static final int EXPORT_DIR_DATA = 0;//导出到data分区
    public static final int EXPORT_DIR_SDCARD_DATA = 1;//导出到sdcard的data
    public static final int EXPORT_DIR_SDCARD = 2;//导出到sdcard根目录

    public static final int DONE = 100;//完成
    public static final int ERROR = 101;//失败
    public static final int FILE_NOT_EXIST = 102;//文件不存在
    public static final int FILE_EXIST = 103;//文件已存在
    public static final int DIR_NOT_EXIST = 104;//目录不存在
    public static final int MAKE_DIR_ERROR = 105;//创建目录失败

    private static File EXPORT_APK_DIR;

    /**
     * 私有构造函数保证调用静态方法
     */
    private JanYoFileUtil() {
    }

    /**
     * 初始化存储的临时目录
     */
    private static void initExportDir() {
        switch (Settings.getExportDir()) {
            case 0:
                EXPORT_APK_DIR = new File(APP.getContext().getFilesDir(), JANYO_SHARE);
                break;
            case 1:
                EXPORT_APK_DIR = new File(APP.getContext().getExternalFilesDir(null), JANYO_SHARE);
                break;
            case 2:
                EXPORT_APK_DIR = new File(Environment.getExternalStorageDirectory(), JANYO_SHARE);
                break;
            default:
                throw new NullPointerException("存储位置错误");
        }
    }

    /**
     * 清理临时目录下文件
     */
    public static int cleanFileDir() {
        initExportDir();
        if (!EXPORT_APK_DIR.exists()) {
            boolean mkdirs = EXPORT_APK_DIR.mkdirs();
            Logs.i(TAG, "cleanFileDir: 创建导出临时目录: " + mkdirs);
            if (!mkdirs)
                return MAKE_DIR_ERROR;
        }
        if (EXPORT_APK_DIR.isDirectory()) {
            for (File file : EXPORT_APK_DIR.listFiles())
                Logs.i(TAG, "cleanFileDir: fileName: " + file.getName() + " deleteResult: " + file.delete());
            return DONE;
        }
        return ERROR;
    }

    /**
     * 通过路径获取文件扩展名
     *
     * @param filePath 文件路径
     * @return 扩展名
     */
    public static String getExtensionFileName(String filePath) {
        int dot = filePath.lastIndexOf('.');
        if (dot > -1 && dot < filePath.length() - 1)
            return filePath.substring(dot + 1);
        return "";
    }

    public static String appendExtensionFileName(String filePath) {
        String extensionFileName = getExtensionFileName(filePath);
        return extensionFileName.length() == 0 ? "" : '.' + extensionFileName;
    }

    /**
     * 获取导出目录绝对路径
     *
     * @return 路径
     */
    public static String getExportDirPath() {
        initExportDir();
        return EXPORT_APK_DIR.getAbsolutePath();
    }

    /**
     * 获取导出目录，返回File对象
     *
     * @return File对象
     */
    public static File getExportDirFile() {
        return new File(getExportDirPath());
    }

    /**
     * 获取指定软件默认导出绝对路径
     *
     * @param installAPP 指定的软件
     * @return 绝对路径
     */
    public static String getExportFilePath(InstallAPP installAPP) {
        String extendFileName = getExtensionFileName(installAPP.getSourceDir());
        return getExportDirPath() + File.separator + formatName(installAPP, Settings.getRenameFormat()) + (extendFileName.length() == 0 ? "" : '.' + extendFileName);
    }

    /**
     * 同上，返回类型为File
     *
     * @param installAPP 指定的软件
     * @return File对象
     */
    public static File getExportFile(InstallAPP installAPP) {
        return new File(getExportFilePath(installAPP));
    }

    /**
     * 获取导出目录下指定文件
     *
     * @param fileNameWithExtends 文件名（含扩展名）
     * @return File对象
     */
    public static File getFile(String fileNameWithExtends) {
        return new File(getExportDirFile(), fileNameWithExtends);
    }

    /**
     * 判断导出目录是否存在
     *
     * @return boolean
     */
    public static boolean isExportDirExist() {
        initExportDir();
        return EXPORT_APK_DIR.exists() || EXPORT_APK_DIR.mkdirs();
    }

    /**
     * 导出指定软件到导出目录，未重命名
     *
     * @param installAPP 制定软件
     * @return 返回码
     */
    public static int exportAPK(InstallAPP installAPP) {
        if (!isExportDirExist())
            return DIR_NOT_EXIST;
        String oldName = installAPP.getSourceDir().substring(installAPP.getSourceDir().lastIndexOf(File.separator) + 1);
        String outputPath = EXPORT_APK_DIR.getAbsolutePath() + File.separator + formatName(installAPP, Settings.getRenameFormat()) + appendExtensionFileName(oldName);
        Logs.i(TAG, "exportAPK: oldName: " + oldName);
        Logs.i(TAG, "exportAPK: outputPath: " + outputPath);
        return copyFile(installAPP.getSourceDir(), outputPath);
    }

    public static int renameFile(InstallAPP installAPP, String fileName) {
        File exportFile = getExportFile(installAPP);
        if (!exportFile.exists())
            return FILE_NOT_EXIST;
        File newFile = new File(getExportDirFile(), fileName + appendExtensionFileName(installAPP.getSourceDir()));
        return exportFile.renameTo(newFile) ? DONE : ERROR;
    }

    public static void doShareFile(Context context, List<File> fileList) {
        ArrayList<Uri> uriList = new ArrayList<>();
        for (File file : fileList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                uriList.add(FileProvider.getUriForFile(context, context.getString(R.string.authorities), file));
            else
                uriList.add(Uri.fromFile(file));
        }
        doShare(context, uriList);
    }

    public static void doShare(Context context, ArrayList<Uri> uriList) {
        Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
        share.setType("*/*");
        share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        context.startActivity(Intent.createChooser(share, context.getString(R.string.title_activity_share)));
    }

    public static void share(Context context, File file) {
        Uri uri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? FileProvider.getUriForFile(context, context.getString(R.string.authorities), file) : Uri.fromFile(file);
        share(context, uri);
    }

    public static void share(Context context, Uri uri) {
        Logs.i(TAG, "share: " + uri);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("*/*");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        grantUriPermission(context, share, uri);
        context.startActivity(Intent.createChooser(share, context.getString(R.string.title_activity_share)));
    }

    private static void grantUriPermission(Context context, Intent intent, Uri uri) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : list) {
            context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static int copyFile(String inputPath, String outputPath) {
        if (!(new File(inputPath)).exists()) {
            Logs.e(TAG, "copyFile: 输入文件不存在");
            return FILE_NOT_EXIST;
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
            return DONE;
        } catch (Exception e) {
            Logs.wtf(TAG, "copyFile: ", e);
            return ERROR;
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
        File file = new File(context.getExternalCacheDir(), fileName);
        return saveObject(list, file);
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

    public static String formatName(InstallAPP installAPP, String format) {
        StringBuilder fileName = new StringBuilder();
        int index = 0;
        while (index < format.length())
            if (format.charAt(index) == '%' && index + 1 < format.length())
                switch (format.charAt(index + 1)) {
                    case 'N':
                        fileName.append(installAPP.getName());
                        index += 2;
                        break;
                    case 'V':
                        fileName.append(installAPP.getVersionName());
                        index += 2;
                        break;
                    case 'W':
                        fileName.append(installAPP.getVersionCode());
                        index += 2;
                        break;
                    case 'P':
                        fileName.append(installAPP.getPackageName());
                        index += 2;
                        break;
                    default:
                        fileName.append('%');
                        index++;
                        break;
                }
            else {
                fileName.append(format.charAt(index));
                index++;
            }
        return fileName.toString();
    }
}
