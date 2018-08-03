package com.rtmap.experience.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class DTImageUtil {
    private final static String TAG = "ImgUtil";

    // 保存Bitmap为图片文件
    public static boolean saveFile(Bitmap bitmap, String fileName) {
        boolean ret = true;
        try {
            File savePath = new File(fileName); // 保存路径
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos); // 质量压缩为80%
            bos.flush();
            bos.close();
        } catch (Exception e) {
        	e.printStackTrace();
            ret = false;
        }
        return ret;
    }
    
    public static boolean saveFile(InputStream stream, String fileName) {
        Bitmap bitmap = BitmapFactory.decodeStream(stream);
        return saveFile(bitmap, fileName);
    }

    /***
     * 加载本地图片
     * @param context：主运行函数实例
     * @param bitAdress：图片地址，一般指向R下的drawable目录
     * @return
     */
    public final Bitmap CreatImage(Context context, int bitAdress) {
        Bitmap bitmaptemp = null;
        bitmaptemp = BitmapFactory.decodeResource(context.getResources(), bitAdress);
        return bitmaptemp;
    }

    // 直接載入圖片
    public static Bitmap getBitmap(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return null;
            }

            BitmapFactory.Options opts = new Options();
            opts.inPreferredConfig = Config.RGB_565;
            Bitmap bt = BitmapFactory.decodeFile(path, opts);
            return bt;
        } catch (Exception e) {
            return null;
        }
    }

    // 2.图片平均分割方法，将大图平均分割为N行N列，方便用户使用
    /***
     * 图片分割
     * @param g
     *        ：画布
     * @param paint
     *        ：画笔
     * @param imgBit
     *        ：图片
     *        x
     *        ：X轴起点坐标
     * @param y
     *        ：Y轴起点坐标
     * @param w
     *        ：单一图片的宽度
     * @param h
     *        ：单一图片的高度
     * @param line
     *        ：第几列
     * @param row
     *        ：第几行
     */
    public final void cuteImage(Canvas g, Paint paint, Bitmap imgBit, int x, int y, int w, int h, int line, int row) {
        g.clipRect(x, y, x + w, h + y);
        g.drawBitmap(imgBit, x - line * w, y - row * h, paint);
        g.restore();
    }

    // 3.图片缩放，对当前图片进行缩放处理
    /***
     * 图片的缩放方法
     * * @param bgimage
     * ：源图片资源
     * @param newWidth
     *        ：缩放后宽度
     * @param newHeight
     *        ：缩放后高度
     * @return
     */
    public Bitmap zoomImage(Bitmap bgimage, int newWidth, int newHeight) {
        // 获取这个图片的宽和高
        int width = bgimage.getWidth();
        int height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    // 4.绘制带有边框的文字，一般在游戏中起文字的美化作用
    /**
     * 绘制带有边框的文字
     * @param strMsg
     *        ：绘制内容
     * @param g
     *        ：画布
     * @param paint
     *        ：画笔
     * @param setx
     *        ：X轴起始坐标
     * @param sety
     *        ：Y轴的起始坐标
     * @param fg
     *        ：前景色
     * @param bg
     *        ：背景色
     */
    public void drawText(String strMsg, Canvas g, Paint paint, int setx, int sety, int fg, int bg) {
        paint.setColor(bg);
        g.drawText(strMsg, setx + 1, sety, paint);
        g.drawText(strMsg, setx, sety - 1, paint);
        g.drawText(strMsg, setx, sety + 1, paint);
        g.drawText(strMsg, setx - 1, sety, paint);
        paint.setColor(fg);
        g.drawText(strMsg, setx, sety, paint);
        g.restore();
    }
    /**  
     * Drawable转化为Bitmap  
     */    
     public static Bitmap drawableToBitmap(Drawable drawable) {    
        int width = drawable.getIntrinsicWidth();    
        int height = drawable.getIntrinsicHeight();    
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);    
        Canvas canvas = new Canvas(bitmap);    
        drawable.setBounds(0, 0, width, height);    
        drawable.draw(canvas);    
        return bitmap;    
         
     } 
}