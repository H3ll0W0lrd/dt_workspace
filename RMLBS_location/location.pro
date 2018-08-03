-injars \\psf\Home\Desktop\rtmap_lbs_location.jar
-outjars \\psf\Home\Desktop\LBS°²×¿-×ÊÁÏ

-libraryjars \\psf\Home\Downloads\eclipse\sdk\platforms\android-19\android.jar
-libraryjars \\psf\Home\Downloads\eclipse\sdk\extras\android\support\v4\android-support-v4.jar
-libraryjars \\psf\Home\Desktop\rtmap_lbs_common.jar

-dontshrink
-dontoptimize
-useuniqueclassmembernames
-keeppackagenames
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod
-keepparameternames
-dontwarn org.xmlpull.v1.**
-ignorewarnings


-keep public class com.rtm.location.LocationApp {
    <fields>;
    <methods>;
}

-keep public class com.rtm.location.JNILocation {
    <fields>;
    <methods>;
}

-keep public class com.rtm.location.common.* {
    <fields>;
    <methods>;
}

-keep public class com.rtm.location.entity.* {
    <fields>;
    <methods>;
}

-keep public class com.rtm.location.utils.* {
    <fields>;
    <methods>;
}

-keep public class * extends android.app.Service

-keep public class * extends com.rtm.common.utils.RMCallBack

-keep public class * extends android.os.AsyncTask

-keep public class * extends com.rtm.common.utils.RMAsyncTask
