package com.lhy.xfif;

import static com.lhy.xfif.MainActivity.ReadText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * FIF 口语训练学生版分数修改 HOOK 3.0
 * @author lhy 1595901624
 * @updater GrandThief
 */
public class HookUpload implements IXposedHookLoadPackage {
    // 获取随机数
    public int random_num(int min, int max) { return new Random().nextInt(max - min + 1) + min; }
    private int[] Range = {80, 95};
    final boolean[] IsFirst = {false};
    @SuppressLint("SdCardPath") String ScorePath = "/sdcard/Android/.xfif_config.lhy", PassPath = "/sdcard/Android/.xfif_autopass";
    // 读写权限
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    // 请求状态码
    private static final int REQUEST_PERMISSION_CODE = 1;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        // 加载自带的ClassLoader
        if (lpparam.packageName.equals("com.fifedu.tsdx")) {
            XposedHelpers.findAndHookMethod("com.stub.StubApp", lpparam.classLoader, "attachBaseContext", Context.class, new XC_MethodHook() {
                @RequiresApi(api = Build.VERSION_CODES.O) @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    // 获取到Context对象，通过这个对象来获取classloader
                    Context context = (Context) param.args[0];
                    // 获取classloader，之后hook加固后的就使用这个classloader
                    ClassLoader classLoader = context.getClassLoader();

                    XposedHelpers.findAndHookMethod("android.security.NetworkSecurityPolicy", lpparam.classLoader, "getInstance", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Object networkSecurityPolicy = param.getResult();
                            XposedHelpers.findAndHookMethod(networkSecurityPolicy.getClass(), "isCleartextTrafficPermitted", String.class, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    param.setResult(true);
                                }
                            });
                        }
                    });

                    // Get the NetworkSecurityPolicy instance
                    // Object networkSecurityPolicy = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.security.NetworkSecurityPolicy", classLoader), "getInstance");
                    // XposedBridge.log("XFIF-" + (boolean) XposedHelpers.callMethod(networkSecurityPolicy, "isCleartextTrafficPermitted", "https://baidu.com"));
                    // XposedBridge.log("XFIF-" + (boolean) XposedHelpers.callMethod(networkSecurityPolicy, "isCleartextTrafficPermitted", "http://verify.mqiu.xyz:88/extend/web/api/web_utils.php?Name=XFIF_Docs"));

                    try {
                        // XposedBridge.log("XFIF-Ready to edit init targetSdkVersion");
                        XposedHelpers.findAndHookMethod("android.app.ApplicationPackageManager", classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                PackageInfo pi = (PackageInfo) param.getResult();
                                if (pi != null && pi.packageName.equals("com.fifedu.tsdx")) {
                                    // XposedBridge.log("XFIF-" + lpparam.appInfo.targetSdkVersion);
                                    pi.applicationInfo.targetSdkVersion = 26; // Set your desired targetSdkVersion here
                                    // XposedBridge.log("XFIF-" + lpparam.appInfo.targetSdkVersion);
                                }
                            }
                        });
                    } catch (Exception ignored) {}

                    // 获取读写
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                            ActivityCompat.requestPermissions(Objects.requireNonNull(findActivity(context)), PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);

                    // 获取自定范围
                    if (new File(ScorePath).exists()) {
                        String[] RangeText = ReadText(ScorePath).split("~");
                        Range = new int[]{Integer.parseInt(RangeText[0]), Integer.parseInt(RangeText[1])};
                    }

                    // 获取用户UserId
                    // (6.5.5+可用)
                    XposedHelpers.findAndHookMethod("com.fifedu.kyxl_library_base.entity.UserInfo", classLoader, "getLoginName", new XC_MethodHook() {
                        @RequiresApi(api = Build.VERSION_CODES.O) @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            super.afterHookedMethod(methodHookParam);
                        }
                    });

                    // 修改版本号 去除更新
                    // (6.5.5+可用)
                    XposedHelpers.findAndHookMethod("com.fifedu.kyxl_library_base.utils.BaseCommonUtils", classLoader, "getVersionName", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            super.afterHookedMethod(methodHookParam);
                            if (!IsFirst[0]) {
                                // XposedBridge.log("XFIF-版本号 BeforeHook: "+ methodHookParam.getResult());
                                methodHookParam.setResult("9999.999.999");
                                // XposedBridge.log("XFIF-版本号 AfterHook: "+ methodHookParam.getResult());
                            }
                        }
                    });

                    // 去除题目前倒计时
                    // (6.5.5+可用)
                    Class<?> OnCountDownCallBack = XposedHelpers.findClass("com.fifedu.kyxl_library_study.ui.view.custom.questioninfo.OnCountDownCallBack",classLoader);
                    Class<?> ChallengeCountDownView = XposedHelpers.findClass("com.fifedu.kyxl_library_study.ui.view.custom.challenge.ChallengeCountDownView",classLoader);
                    XposedHelpers.findAndHookConstructor("com.fifedu.kyxl_library_study.ui.view.custom.challenge.ChallengeCountDownView$1", classLoader,
                            ChallengeCountDownView , long.class, long.class, OnCountDownCallBack, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    super.beforeHookedMethod(methodHookParam);
                                    if (!IsFirst[0]) {
                                        // XposedBridge.log("XFIF-Haha, repleaceFunc are replaced");
                                        methodHookParam.args[1] = 100;
                                        methodHookParam.args[2] = 10000;
                                        // XposedHelpers.callMethod(methodHookParam.thisObject, "cancel");
                                    }
                                }
                            }
                    );

                    // 获取题目数量
                    // (6.5.5+可用)
//                    XposedHelpers.findAndHookMethod("com.fifedu.kyxl_library_study.bean.study.checkpoint.Item", classLoader, "getQuestions", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                            super.afterHookedMethod(methodHookParam);
//                            // XposedBridge.log("XFIF-题目数量 AfterHook: "+methodHookParam.getResult());
//                        }
//                    });

                    // 获取题目录音倒计时
                    // (6.5.5+可用)
                    XposedHelpers.findAndHookMethod("com.fifedu.kyxl_library_study.ui.view.custom.challenge.ChallengeEvaluateCountDownView", classLoader, "startCountDown", int.class, new XC_MethodHook() {
                        @SuppressLint("SdCardPath")
                        @Override protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            super.beforeHookedMethod(methodHookParam);
                            if (!IsFirst[0]) {
                                if (new File(PassPath).exists())
                                    methodHookParam.args[0] = 1;
                            }
                            // XposedBridge.log("XFIF-startCountdown ChallengeEvaluateCountDownView AfterHook: " + methodHookParam.args[0]);
                        }
                    });

                    // 修改总成绩 语义 流利 准确度 完整度 语调
                    // (6.5.5+可用)
                    String[] strArr = {"getScore", "getSemantic", "getFluency", "getAccuracy", "getComplete", "getToneScore"};
                    for (String s : strArr) {
                        XposedHelpers.findAndHookMethod("com.fifedu.kyxl_library_study.bean.result.AnswerResultInfo", classLoader, s, new XC_MethodHook() {
                            @Override protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                super.beforeHookedMethod(methodHookParam);
                                if (!IsFirst[0]) {
                                    if (Range[0] >= 0 && Range[1] <= 100)
                                        methodHookParam.setResult(String.valueOf(random_num(Range[0], Range[1])));
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private static Activity findActivity(@NonNull Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivity(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }
}