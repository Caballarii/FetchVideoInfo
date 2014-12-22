package com.derek.videoinfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class VideoUtils {

	public static void main(String[] args){
		System.out.println("123");
	}
	
	public static String[] getVideoInfo(String url){
		String regForURL="(youku.com|tudou.com|v.qq.com|letv.com)";
		Pattern pUrl = Pattern.compile(regForURL);
		Matcher mUrl = pUrl.matcher(url);
		if(mUrl.find()){
			String match=mUrl.group();
			if(match.equals("youku.com")){
				return parseYouku(url);
			}
			else if(match.equals("tudou.com")){
				return parseTudou(url);
			}
			else if(match.equals("v.qq.com")){
				return parseQQ(url);
			}
			else if(match.equals("letv.com")){
				return parseLetv(url);
			}
		}
		return null;
	}
	
	private static String[] parseYouku(String url){
		String[] result=new String[2];
		String id="";
		if(url.indexOf("sid/")!=-1){
			id=url.substring(url.indexOf("sid/")+4,url.indexOf("/v.swf"));
		}
		else{
			Pattern p = Pattern.compile("(?<=id_)(.*)(?=.html)");
			Matcher m = p.matcher(url);
			if(m.find()){
				id=m.group();
			}
			else{
				return null;
			}
		}
		
		JSONArray arr=new JSONArray();
		try {
			URL myurl = new URL("http://v.youku.com/player/getPlayList/VideoIDS/"+id+"/timezone/+08/version/5/source/out?password=&ran=2513&n=3");
			InputStreamReader isr = new InputStreamReader(myurl.openStream());
			BufferedReader br = new BufferedReader(isr);
			String urls = br.readLine();
			br.close();
			JSONObject json = JSONObject.fromObject(urls);
			arr = json.getJSONArray("data");
			if(arr.size()==0){
				return null;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		result[0]="http://player.youku.com/player.php/sid/"+id+"/v.swf";
		result[1]=JSONObject.fromObject(arr.get(0)).get("logo").toString();
		return result;
	}
	
	private static String[] parseTudou(String url){
		String[] result=new String[2];
		String id="";
		String iid="";
		if(url.indexOf("view/")!=-1){
			Pattern p = Pattern.compile("(?<=view/)(.*)(?=/)");
			Matcher m = p.matcher(url);
			if(m.find()){
				id=m.group();
			}
			else{
				return null;
			}
			result[0]="http://www.tudou.com/v/"+id+"/v.swf";
			String html=getSocketContent("www.tudou.com","/v/"+id+"/v.swf");
			Pattern pImage = Pattern.compile("(?<=snap_pic=)(.*jpg)(?=&code=)");
			Matcher mImage = pImage.matcher(html);
			if(mImage.find()){
				result[1]=mImage.group();
			}
			else{
				result[1]="";
			}
			return result;
		}
		else if(url.indexOf("albumplay")!=-1){						
			String html=getUrlContent(url);
			if(html==null){
				return null;
			}
			Pattern pId =Pattern.compile("(?<=lcode:.')(.*)(?=')");
			Matcher mId =pId.matcher(html);
			if(mId.find()){
				id=mId.group();
			}
			else{
				return null;
			}
			Pattern pIId = Pattern.compile("(?<=iid:.)([0-9]*)");
			Matcher mIId = pIId.matcher(html);
			if(mIId.find()){
				iid=mIId.group();
			}
			else{
				return null;
			}
			result[0]="http://www.tudou.com/a/"+id+"/&iid="+iid+"/v.swf";
			Pattern pImage = Pattern.compile("(?<=pic:.')(.*)(?=')");
			Matcher mImage = pImage.matcher(html);						
			if(mImage.find()){
				result[1]=mImage.group();
			}
			else{
				result[1]="";
			}
			return result;
		}
		else if(url.indexOf("listplay")!=-1){
			String html=getUrlContent(url);
			if(html==null){
				return null;
			}
			Pattern pId =Pattern.compile("(?<=lcode:.')(.*)(?=')");
			Matcher mId =pId.matcher(html);
			if(mId.find()){
				id=mId.group();
			}
			else{
				return null;
			}
			Pattern pIId = Pattern.compile("(?<=iid:.)([0-9]*)");
			Matcher mIId = pIId.matcher(html);
			if(mIId.find()){
				iid=mIId.group();
			}
			else{
				return null;
			}
			result[0]="http://www.tudou.com/l/"+id+"/&iid="+iid+"/v.swf";
			Pattern pImage = Pattern.compile("(?<=pic:.')(.*)(?=')");
			Matcher mImage = pImage.matcher(html);						
			if(mImage.find()){
				result[1]=mImage.group();
			}
			else{
				result[1]="";
			}
			return result;
		}
		return null;
	}
	
	private static String[] parseQQ(String url){
		String[] result=new String[2];
		String id="";
		String vid="";
		String html=getUrlContent(url);
		Pattern pVid = Pattern.compile("(?<=vid:\")(.*)(?=\",)");
		Matcher mVid = pVid.matcher(html);
		if(mVid.find()){
			vid=mVid.group();
		}
		else{
			return null;
		}
		Pattern pId = Pattern.compile("(?<=/)([^/]*)(?=/"+vid+".html)");
		Matcher mId = pId.matcher(html);
		if(mId.find()){
			id=mId.group();
		}
		else{
			return null;
		}
		result[0]="http://imgcache.qq.com/tencentvideo_v1/player/TencentPlayer.swf?vid="+vid;
		result[1]="http://vpic.video.qq.com/"+id+"/"+vid+"_1.jpg";
		return result;
	}
	
	private static String[] parseLetv(String url){
		String[] result=new String[2];
		String id="";
		String html=getUrlContent(url);
		Pattern pId = Pattern.compile("(?<=vplay/)([0-9]*)");
		Matcher mId = pId.matcher(url);
		if(mId.find()){
			result[0]="http://www.letv.com/player/x"+mId.group()+".swf";
		}
		else{
			return null;
		}
		Pattern pImage = Pattern.compile("(?<=pic:\")([^\"]*)(?=[^}]*"+id+")");
		Matcher mImage = pImage.matcher(html);
		if(mImage.find()){
			result[1]=mImage.group();
		}
		else{
			return null;
		}
		return result;
	}
	
	private static String getSocketContent(String host,String path){
        String result="";
        try
        {
            int port = 80;
            InetAddress addr = InetAddress.getByName(host);
            
            Socket socket = new Socket(addr, port);

            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            wr.write("GET " + path + " HTTP/1.0\r\n");
            wr.write("HOST:" + host + "\r\n");
            wr.write("Accept:*/*\r\n");
            wr.write("\r\n");
            wr.flush();
            
            BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null)
            {
            		result+=line;
            }
            wr.close();
            rd.close();
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
		return null;
	}
	
	private static String getUrlContent(String urlStr){
		try
        {
            URL url = new URL(urlStr);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            
            String s = "";
            
            StringBuffer sb = new StringBuffer();
            
            while ((s = br.readLine()) != null)
            {
                sb.append(s + "\r\n");
            }
            
            return sb.toString();
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return null;
	}
}
