package com.example.wechat.Utils;

import android.text.TextUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

public class MimeUtils {
    public static HashMap<String, String> getMimeMap() {
        HashMap<String, String> mapSimple = new HashMap<>();
        if (mapSimple.size() == 0) {

            mapSimple.put(".3gp", "video/3gpp");
            mapSimple.put(".asf", "video/x-ms-asf");
            mapSimple.put(".avi", "video/x-msvideo");
            mapSimple.put(".m4u", "video/vnd.mpegurl");
            mapSimple.put(".m4v", "video/x-m4v");
            mapSimple.put(".mov", "video/quicktime");
            mapSimple.put(".mp4", "video/mp4");
            mapSimple.put(".mpe", "video/mpeg");
            mapSimple.put(".mpeg", "video/mpeg");
            mapSimple.put(".mpg", "video/mpeg");
            mapSimple.put(".mpg4", "video/mp4");

            mapSimple.put(".apk", "application/vnd.android.package-archive");
            mapSimple.put(".bin", "application/octet-stream");
            mapSimple.put(".chm", "application/x-chm");
            mapSimple.put(".class", "application/octet-stream");
            mapSimple.put(".doc", "application/msword");
            mapSimple.put(".docx", "application/msword");
            mapSimple.put(".exe", "application/octet-stream");
            mapSimple.put(".gtar", "application/x-gtar");
            mapSimple.put(".gz", "application/x-gzip");
            mapSimple.put(".jar", "application/java-archive");
            mapSimple.put(".js", "application/x-javascript");
            mapSimple.put(".mpc", "application/vnd.mpohun.certificate");
            mapSimple.put(".msg", "application/vnd.ms-outlook");
            mapSimple.put(".pps", "application/vnd.ms-powerpoint");
            mapSimple.put(".ppt", "application/vnd.ms-powerpoint");
            mapSimple.put(".pptx", "application/vnd.ms-powerpoint");
            mapSimple.put(".pdf", "application/pdf");
            mapSimple.put(".xls", "application/vnd.ms-excel");
            mapSimple.put(".xlsx", "application/vnd.ms-excel");
            mapSimple.put(".z", "application/x-compress");
            mapSimple.put(".zip", "application/zip");
            mapSimple.put(".rar", "application/x-rar-compressed");
            mapSimple.put(".tar", "application/x-tar");
            mapSimple.put(".tgz", "application/x-compressed");
            mapSimple.put(".rtf", "application/rtf");
            mapSimple.put(".wps", "application/vnd.ms-works");

            mapSimple.put(".jpeg", "image/jpeg");
            mapSimple.put(".jpg", "image/jpeg");
            mapSimple.put(".gif", "image/gif");
            mapSimple.put(".bmp", "image/bmp");
            mapSimple.put(".png", "image/png");

            mapSimple.put(".c", "text/plain");
            mapSimple.put(".conf", "text/plain");
            mapSimple.put(".cpp", "text/plain");
            mapSimple.put(".h", "text/plain");
            mapSimple.put(".htm", "text/html");
            mapSimple.put(".html", "text/html");
            mapSimple.put(".java", "text/plain");
            mapSimple.put(".log", "text/plain");
            mapSimple.put(".xml", "text/plain");
            mapSimple.put(".prop", "text/plain");
            mapSimple.put(".rc", "text/plain");
            mapSimple.put(".sh", "text/plain");
            mapSimple.put(".txt", "text/plain");

            mapSimple.put(".m3u", "audio/x-mpegurl");
            mapSimple.put(".m4a", "audio/mp4a-latm");
            mapSimple.put(".m4b", "audio/mp4a-latm");
            mapSimple.put(".m4p", "audio/mp4a-latm");
            mapSimple.put(".mp2", "audio/x-mpeg");
            mapSimple.put(".mp3", "audio/x-mpeg");
            mapSimple.put(".mpga", "audio/mpeg");
            mapSimple.put(".ogg", "audio/ogg");
            mapSimple.put(".wav", "audio/x-wav");
            mapSimple.put(".wma", "audio/x-ms-wma");
            mapSimple.put(".wmv", "audio/x-ms-wmv");
            mapSimple.put(".rmvb", "audio/x-pn-realaudio");

            mapSimple.put("", "*/*");
        }
        return mapSimple;
    }

    public static String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex > 0) {
            //获取文件的后缀名
            String end = fName.substring(dotIndex, fName.length()).toLowerCase(Locale.getDefault());
            //在MIME和文件类型的匹配表中找到对应的MIME类型。
            HashMap<String, String> map = getMimeMap();
            if (!TextUtils.isEmpty(end) && map.keySet().contains(end)) {
                type = map.get(end);
            }
        }
        return type;
    }
}
