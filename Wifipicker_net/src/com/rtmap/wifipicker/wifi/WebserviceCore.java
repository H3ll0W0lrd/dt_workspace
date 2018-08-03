package com.rtmap.wifipicker.wifi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.rtmap.wifipicker.core.WPApplication;
import com.rtmap.wifipicker.util.Constants;
import com.rtmap.wifipicker.util.DTFileUtils;
import com.rtmap.wifipicker.util.FileUtil;

public class WebserviceCore {
    private static final String TAG = "LocationWebservice";
    private static final String SOAP_ACTION = "http://WebXml.com.cn/getMobileCodeInfo";
    private static final String METHOD_NAME_NET_RESULT = "getNetResult";
    private static final String NAMESPACE = "http://tempuri.org/ns.xsd";

    public static String getNetResult(String inputXml, String serviceIp) {
        String ret = "";
        String logPath = String.format("%s%s.txt", Constants.WIFI_PICKER_PATH, WPApplication.getInstance().getShare()
				.getString(DTFileUtils.PREFS_USERNAME, ""));
        String serverType = WPApplication.getInstance().getShare().getString("server_type", "0");
        if (serverType.equals("0")) {
            ret = getNetResultWindows(inputXml, serviceIp, logPath);
        } else {
            ret = getNetResultLinux(inputXml, serviceIp, logPath);
        }
        return ret;
    }

    private static String getNetResultWindows(String inputXml, String serviceIp, String logPath) {
        String ret = "";
        try {
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME_NET_RESULT);// 命名空间和方法名
            request.addProperty("inXml", inputXml);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            String urlString = "http://" + serviceIp + ":" + NetGatherData.sPORT + "/";
            FileUtil.fstream(logPath, "IN:" + "\n" + urlString + "\n" + inputXml + "\n");
            HttpTransportSE androidHttpTransport = new HttpTransportSE(urlString, 8000);// 2000ms为网络延时
            androidHttpTransport.call(SOAP_ACTION, envelope);
            Object result = (Object) envelope.getResponse();
            ret = result.toString();
            FileUtil.fstream(logPath, "OUT:" + "\n" + ret + "\n");
        } catch (Exception e) {
        	e.printStackTrace();
            FileUtil.fstream(logPath, "Exception:" + "\n" + e.toString() + "\n" + ret + "\n");
        }
        return ret;
    }

    private static String getNetResultLinux(String inputXml, String serviceIp, String logPath) {
        String ret = "";
        try {
            Socket socket = new Socket(serviceIp, Integer.parseInt(NetGatherData.sPORT));
            socket.setSoTimeout(8000);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                    true);
            out.println(inputXml);
            FileUtil.fstream(logPath, "request:" + serviceIp + ":" + NetGatherData.sPORT + "\t" + inputXml + "\n");
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            char[] buf = new char[2048];
            int size = br.read(buf);
            for (int i = 0; i < size; i++) {
                ret += buf[i];
            }
            FileUtil.fstream(logPath, "response:" + ret + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            FileUtil.fstream(logPath, "Exception:" + "\n" + e.toString() + "\n" + ret + "\n");
        }
        return ret;
    }

}
