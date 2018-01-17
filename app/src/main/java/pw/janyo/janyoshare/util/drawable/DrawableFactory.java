package pw.janyo.janyoshare.util.drawable;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import vip.mystery0.tools.logs.Logs;

public class DrawableFactory {
    private static final String TAG = "DrawableFactory";
    private DrawableConvertContext drawableConvertContext = new DrawableConvertContext();

    public boolean save(Drawable drawable, String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            boolean mkdirs = file.getParentFile().mkdirs();
            if (!mkdirs) {
                Logs.e(TAG, "save: 创建文件夹失败");
                return false;
            }
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            Bitmap bitmap = drawableConvertContext.convert(drawable);
            if (bitmap == null)
                return false;
            //图片裁剪在这里调用
            bitmap.compress(Bitmap.CompressFormat.PNG, 1, fileOutputStream);
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
}
