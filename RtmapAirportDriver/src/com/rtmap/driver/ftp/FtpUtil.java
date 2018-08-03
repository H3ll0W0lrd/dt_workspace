package com.rtmap.driver.ftp;

import com.rtmap.driver.util.FileUtil;
import com.rtmap.driver.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liyan on 15/12/9.
 */
public class FtpUtil {
    private static FtpUtil incObj;

    private final String host = "123.56.138.28";

    private final int port = 21;

    private final String userName = "airport";

    private final String passWord = "1234567";

    private Ftp ftp;

    public static FtpUtil instance() {
        if (incObj == null) {
            incObj = new FtpUtil();
        }
        return incObj;
    }


    public void upLoadLog(final Ftp.Config config ,Ftp.UploadProgressListener listener) {
        if (ftp == null) {
            ftp = new Ftp(config);
        }

        List<String> dirs = FileUtil.getLogDirs();
        String diverRootDir  = FileUtil.getAppDiverRootDir();
        String content = "";
        try {
//            for (String dir : dirs) {
//                ftp.uploadMultiFile( getFileListByDir(dir),dir,listener);
//            }

            ftp.uploadMultFilesInDir(getDirFileObjListByDirs(dirs), listener, new Ftp.UploadFileListener() {
                @Override
                public void onUploadFileFinish(boolean isSuccess, File file) {
                    String content = "";
                    if (isSuccess) {
                        content = "succ";
                    } else {
                        content = "fail";
                    }
                    //        上传IP地址、端口、用户名、密码，上传结果（成功、失败），上传日期和时间等
                    content = config.hostName+"_"+config.serverPort+"_"+config.userName+"_"+config.password+"_"+content+ "_"+TimeUtil.getFormatNowDate("yyyy-MM-dd-HH:mm:ss")+"|"+file.getName();
                    FileUtil.saveFtpUploadToFile(content + "\n");
                }
            });
            content = "succ";
        } catch (IOException e) {
            e.printStackTrace();
            content = "fail";
        }
        if (content.equals("fail")) {
            listener.onUploadProgress("无日志上传",0,null);
        }
        FileUtil.cleanLogDir();
    }

    public void destory(){
        if (ftp != null) {
            try {
                ftp.closeConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ftp = null;
        }
    }

    private LinkedList<File> getFileListByDir(String dir) {
        return FileUtil.getLogFilesByDir(dir);
    }

    private LinkedList<DirFileObj> getDirFileObjListByDirs(List<String> dirs) {
        LinkedList<DirFileObj> dirFileObjs = new LinkedList<DirFileObj>();
        for (String dir : dirs) {
            DirFileObj dirFileObj = new DirFileObj();
            dirFileObj.dirName = dir;
            dirFileObj.filesInDir = FileUtil.getLogFilesByDir(dir);
            dirFileObjs.add(dirFileObj);
        }
        return dirFileObjs;
    }

    private String getUserLogDirName() {
        return FileUtil.getUserLogDirName();
    }
}
