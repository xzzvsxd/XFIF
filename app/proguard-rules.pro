# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

## ---------------------------------------litepal--------------------------------------------------
-keep class org.litepal.** {*;}
-keep class * extends org.litepal.crud.LitePalSupport {*;}
## ---------------------------------------litepal--------------------------------------------------

## ---------------------------------------glide--------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
<init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
**[] $VALUES;
public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
*** rewind();
}
#异常注释
# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
## ---------------------------------------glide--------------------------------------------------

## ---------------------------------------eventbus--------------------------------------------------
-keepattributes *Annotation*
-keepclassmembers class * {
@org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# If using AsyncExecutord, keep required constructor of default event used.
# Adjust the class name if a custom failure event type is used.
-keepclassmembers class org.greenrobot.eventbus.util.ThrowableFailureEvent {
<init>(java.lang.Throwable);
}

# Accessed via reflection, avoid renaming or removal
-keep class org.greenrobot.eventbus.android.AndroidComponentsImpl
## ---------------------------------------eventbus--------------------------------------------------

## ---------------------------------------rxjava--------------------------------------------------
-dontwarn java.util.concurrent.Flow*
## ---------------------------------------rxjava--------------------------------------------------

## ---------------------------------------bugly--------------------------------------------------
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
## ---------------------------------------bugly--------------------------------------------------

#虹软混淆--没有官方配置，可以选择对jar包里面的类进行忽略
-keep class com.arcsoft.face.**{*;}
-keep class com.arcsoft.imageutil.**{*;}

# 设置所有 native 方法不被混淆
-keepclasseswithmembernames class * {
native <methods>;
}

#2.不混淆类
-keep class com.lhy.xfif.HookUpload { *; }
#-keep class com.lhy.xfif.MainActivity { *; }

# 关闭预校验（对 Android 平台无效，建议关闭）
-dontpreverify

#默认配置
# 指定代码的压缩级别
-optimizationpasses 8

# 不忽略库中的非public的类成员
-dontskipnonpubliclibraryclassmembers

# google推荐算法
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

#优化时允许访问并修改有修饰符的类和类的成员
-allowaccessmodification

# 避免混淆Annotation、内部类、泛型、匿名类
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod

#保持泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 保持四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

# 保持support下的所有类及其内部类
-keep class android.support.** {*;}

# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# 保持自定义控件
-keep public class * extends android.view.View{
*** get*();
void set*(***);
public <init>(android.content.Context);
public <init>(android.content.Context, android.util.AttributeSet);
public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保持所有实现 Serializable 接口的类成员
-keepclassmembers class * implements java.io.Serializable {
static final long serialVersionUID;
private static final java.io.ObjectStreamField[] serialPersistentFields;
private void writeObject(java.io.ObjectOutputStream);
private void readObject(java.io.ObjectInputStream);
java.lang.Object writeReplace();
java.lang.Object readResolve();
}


# webView处理
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
public void *(android.webkit.webView, jav.lang.String);
}

#保留model数据 如果不对json数据做处理，在做请求的时候，也会混淆字段，导致请求失败
#-keep **.model*.** {*;}