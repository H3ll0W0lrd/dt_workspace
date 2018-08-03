package com.rtmap.locationcheck.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class WebCommunication {
    private static final String TAG = "WebCommunication";

    /**
     * @param 只发送普通数据
     *            ,调用此方法
     * @param urlString
     *            对应的Php 页面
     * @param params
     *            需要发送的相关数据 包括调用的方法
     * @param filepath
     *            图片或文件手机上的地址 如:sdcard/photo/123.jpg
     * @param fileparam
     *            图片名称
     * @return result 说明：1 成功 </br> 2 失败 </br> 3 服务器异常 </br> 4 客户端异常
     */
    public String uploadFile(String urlString, Map<String, Object> params, String filepath, String fileparam) {
        String result = "";

        String end = "\r\n";
        String uploadUrl = "/";// new BingoApp().URLIN 是我定义的上传URL
        String MULTIPART_FORM_DATA = "multipart/form-data";
        String BOUNDARY = "---------7d4a6d158c9"; // 数据分隔线
        String imguri = "";
        if (filepath != null && !filepath.equals("")) {
            imguri = filepath.substring(filepath.lastIndexOf("/") + 1);// 获得图片或文件名称
        }

        if (!urlString.equals("")) {
            uploadUrl = urlString;

            try {
                URL url = new URL(uploadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);// 允许输入
                conn.setDoOutput(true);// 允许输出
                conn.setUseCaches(false);// 不使用Cache
                conn.setConnectTimeout(6000);// 6秒钟连接超时
                conn.setReadTimeout(6000);// 6秒钟读数据超时
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);

                StringBuilder sb = new StringBuilder();

                // 上传的表单参数部分，格式请参考文章
                for (Map.Entry<String, Object> entry : params.entrySet()) {// 构建表单字段内容
                    sb.append("--");
                    sb.append(BOUNDARY);
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                    sb.append(entry.getValue());
                    sb.append("\r\n");
                }

                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(sb.toString().getBytes());

                if (filepath != null && !filepath.equals("")) {
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + fileparam + "\"; filename=\"" + imguri + "\""
                            + "\r\n" + "Content-Type:*/*\r\n\r\n");
                    FileInputStream fis = new FileInputStream(filepath);
                    byte[] buffer = new byte[1024]; // 8k
                    int count = 0;
                    while ((count = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, count);
                    }
                    dos.writeBytes(end);
                    fis.close();
                }
                dos.writeBytes("--" + BOUNDARY + "--\r\n");
                dos.flush();

                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                result = br.readLine();
            } catch (Exception e) {
                result = "4";
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 得到图片信息流文件
     * 
     * @param u
     *            url
     * @return 文件信息流
     * @throws Exception
     */
    public static InputStream downloadFile(String u) throws Exception {
        URL url = new URL(u);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000); // 设定超时
        conn.setRequestMethod("GET"); // GET为下载，POST为上传
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return conn.getInputStream(); // 获得数据流
        } else {
            return null;
        }
    }
}