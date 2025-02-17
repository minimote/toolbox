# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-keep class cn.minimote.toolbox.objects.ActivityStorageHelper { *; }
#-keep class cn.minimote.toolbox.data_class.StoredActivity { *; }

# 保持 Gson 类及其所有方法
#-keep class com.google.gson.** { *; }

# 保持 TypeToken 及其子类
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class com.google.gson.reflect.TypeToken {
    <methods>;
}
# 保持所有数据类及其字段
-keep class cn.minimote.toolbox.data_class.** {
    <fields>;
}

## 保持所有自定义类及其构造函数，并保留泛型签名
#-keepattributes Signature

## 保持 StoredActivityStorageHelper 类及其所有方法
#-keep class cn.minimote.toolbox.objects.StoredActivityStorageHelper {
#    <methods>;
#}
## 保持 InstalledActivityStorageHelper 类及其所有方法
#-keep class cn.minimote.toolbox.objects.InstalledActivityStorageHelper {
#    <methods>;
#}

## 保持所有 ViewModel 类及其所有方法
#-keep class cn.minimote.toolbox.view_model.ToolboxViewModel {
#    <methods>;
#}

# 保持所有 Fragment 类及其所有方法
-keep class cn.minimote.toolbox.fragment.** {
    <methods>;
}

## 保持所有泛型签名
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    !static !transient <fields>;
#    !private <fields>;
#    !private <methods>;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}

## 保持 TypeToken 使用的类
#-keep class cn.minimote.toolbox.objects.InstalledActivityStorageHelper$* {
#    <init>(...);
#}
#-keep class cn.minimote.toolbox.objects.StoredActivityStorageHelper$* {
#    <init>(...);
#}