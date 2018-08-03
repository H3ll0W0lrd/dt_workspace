package com.minnw.tools;

import org.apache.commons.csts.binary.Base64;


public class Base64Tool {
	
	
	public static String ASCIIToBase64(String value) {
		byte[] data1 = new byte[value.length()];
		for(int i = 0 ; i < value.length(); i  ++){
			data1[i]=(byte) value.charAt(i);
		}
		 return   Base64.encodeBase64String(data1);
	}
	public static   String  HexToBase64(String value) {
		value=value.replaceAll("-", "");
		value=value.replaceAll(":", "");
		if (value.length() % 2!=0) value = "0" + value;
		value = value.toLowerCase();
		int[] data = new int[value.length()/2];
		byte[] data1 = new byte[value.length()/2];
		String  pos = "0123456789abcdef";
		for(int i = 0,j = 0; i < value.length(); i += 2,j++){
			data[j] = (pos.indexOf(value.charAt(i)) << 4) | (pos.indexOf(value.charAt(i + 1)));
			data1[j]=(byte) data[j];
		}
		 return   Base64.encodeBase64String(data1);
	}
	
	public static   String  toHex4(String result){
		   result=Integer.toHexString(Integer.parseInt(result.toString()));
		  while(result.length()<8){
		    result="0"+result;
		  }
		  return result;
		}
	
	public static   String  toHextoUpperCase(String result,int i){
		   result=Integer.toHexString(Integer.parseInt(result.toString()));
		  while(result.length()<i){
		    result="0"+result;
		  }
		  return result.toUpperCase();
		}
	public static   String  toHex(String result){
		   result=Integer.toHexString(Integer.parseInt(result.toString()));
		  while(result.length()<4){
		    result="0"+result;
		  }
		  return result;
		}
	public static   String  toHexMeasuredPower(String result){
		if(Integer.parseInt(result)>=0){
			  result=Integer.toHexString(Integer.parseInt(result.toString()));
			
		}else{
		   int  value1 = 0xFF + Integer.parseInt(result.toString()) + 1;
		   result=Integer.toHexString(value1);
		}
		  while(result.length()<2){
			    result="0"+result;
			  }
		  return result.toUpperCase();
		}
	
}
