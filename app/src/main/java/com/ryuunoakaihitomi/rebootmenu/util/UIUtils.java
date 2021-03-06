package com.ryuunoakaihitomi.rebootmenu.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.Shortcut;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * 本应用关于界面操作的工具集合
 * Created by ZQY on 2018/2/10.
 *
 * @author ZQY
 */

public class UIUtils {

    /**
     * 加载特定主题颜色的AlertDialog
     *
     * @param isWhite      是否白色主题
     * @param activityThis 当前activity的上下文
     * @return 已处理Builder对象
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static AlertDialog.Builder LoadDialog(boolean isWhite, Activity activityThis) {
        //在API级别23中，AlertDialog的主题定义被废弃。用在API级别22中新引入的Android默认主题格式代替。
        boolean isAndroidMPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
        int themeCode;
        if (isWhite) {
            if (isAndroidMPlus)
                themeCode = android.R.style.Theme_DeviceDefault_Light_Dialog_Alert;
            else
                themeCode = AlertDialog.THEME_DEVICE_DEFAULT_LIGHT;
        } else {
            if (isAndroidMPlus)
                themeCode = android.R.style.Theme_DeviceDefault_Dialog_Alert;
            else
                themeCode = AlertDialog.THEME_DEVICE_DEFAULT_DARK;
        }
        new DebugLog("LoadDialog: themeCode=" + themeCode, DebugLog.LogLevel.V);
        return new AlertDialog.Builder(activityThis, themeCode);
    }

    //半透明级别(alphaShow参数)
    public class TransparentLevel {
        public static final float NORMAL = 0.75f;
        public static final float CONFIRM = 0.9f;
        static final float HELP = 0.8f;
    }

    /**
     * 将窗体透明展示
     * 不建议在Low Ram设备启动此功能
     *
     * @param w 欲透明化的dialog
     * @param f 透明度
     * @throws NullPointerException null.XXX();
     */
    public static void alphaShow(AlertDialog w, Float f) {
        Window window = w.getWindow();
        assert window != null;
        //由于ActivityManager的isLowRamDevice方法非静态，因此只能使用反射来取系统属性了。（但在Android P(ill)上可能行不通）
        boolean isLowRam = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            try {
                @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
                Method method = clazz.getMethod("get", String.class);
                isLowRam = "true".equals(method.invoke(null, "ro.config.low_ram"));
            } catch (Exception e) {
                new DebugLog(e, "alphaShow", true);
            }
        new DebugLog("alphaShow: isLowRam=" + isLowRam, DebugLog.LogLevel.I);
        if (!isLowRam) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = f;
            window.setAttributes(lp);
        }
        w.show();
    }

    //显示帮助对话框
    private static void helpDialog(final Activity activityThis, final AlertDialog.Builder returnTo, boolean cancelable, boolean isWhite) {
        new DebugLog("helpDialog", DebugLog.LogLevel.V);
        new TextToast(activityThis, String.format(activityThis.getString(R.string.help_notice), getAppVersionName(activityThis), activityThis.getString(R.string.help_update_date)));
        AlertDialog.Builder h = LoadDialog(isWhite, activityThis);
        h.setTitle(activityThis.getString(R.string.help));
        String help = inputStream2String(activityThis.getResources().openRawResource(R.raw.help_body), null);
        h.setMessage(Html.fromHtml(help));
        h.setOnCancelListener(p1 -> alphaShow(returnTo.create(), TransparentLevel.NORMAL));
        h.setNeutralButton(activityThis.getString(R.string.offical_download_link), (p1, p2) -> openURL(activityThis, "https://github.com/ryuunoakaihitomi/rebootmenu/releases"));
        h.setNegativeButton(activityThis.getString(R.string.donate), (p1, p2) -> openURL(activityThis, "http://ryuunoakaihitomi.info/donate/"));
        //有意保留的bug:帮助对话框的退出方式与配置相反
        if (cancelable) {
            h.setPositiveButton(activityThis.getString(R.string.exit), (p1, p2) -> alphaShow(returnTo.create(), TransparentLevel.NORMAL));
            h.setCancelable(false);
        }
        //检测调试环境用：测试异常
        AlertDialog hc = h.create();
        alphaShow(hc, TransparentLevel.HELP);
        hc.getButton(DialogInterface.BUTTON_NEGATIVE).setOnLongClickListener(v -> {
            throw new Error("test error");
        });
    }

    //尝试打开URL
    private static void openURL(Context context, String link) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (ActivityNotFoundException e) {
            new TextToast(context, true, link + "\n" + context.getString(R.string.url_open_failed_notice));
        } finally {
            ((Activity) context).finish();
        }
    }

    /**
     * 使状态栏透明
     * 来自https://github.com/laobie/StatusBarUtil
     *
     * @param activity 要渲染的活动
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void transparentStatusBar(Activity activity) {
        new DebugLog("transparentStatusBar", DebugLog.LogLevel.V);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    //通过配置文件选择退出方式和设置帮助按钮
    public static void setExitStyleAndHelp(final Activity context, final AlertDialog.Builder builder) {
        new DebugLog("setExitStyleAndHelp", DebugLog.LogLevel.V);
        //是否需要退出键
        if (!ConfigManager.get(ConfigManager.CANCELABLE))
            builder.setPositiveButton(R.string.exit, (dialogInterface, i) -> context.finish());
        //不按退出的退出监听
        builder.setCancelable(ConfigManager.get(ConfigManager.CANCELABLE));
        builder.setOnCancelListener(p1 -> {
            new TextToast(context.getApplicationContext(), false, context.getString(R.string.exit_notice));
            context.finish();
        });
        //帮助
        builder.setNegativeButton(R.string.help, (dialogInterface, i) ->
                UIUtils.helpDialog(context, builder, ConfigManager.get(ConfigManager.CANCELABLE), ConfigManager.get(ConfigManager.WHITE_THEME)));
    }

    /**
     * 启动器添加快捷方式
     *
     * @param context     上下文
     * @param titleRes    标题资源id
     * @param iconRes     图标资源id
     * @param shortcutAct Shortcut额外
     * @param isForce     是否是root强制模式
     * @see com.ryuunoakaihitomi.rebootmenu.Shortcut
     */
    @SuppressWarnings("ConstantConditions")
    public static void addLauncherShortcut(Context context, int titleRes, int iconRes, int shortcutAct, boolean isForce) {
        new DebugLog("addLauncherShortcut", DebugLog.LogLevel.V);
        String forceToken = isForce ? "*" : "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            context.sendBroadcast(new Intent("com.android.launcher.action.INSTALL_SHORTCUT")
                    .putExtra("duplicate", false)
                    .putExtra(Intent.EXTRA_SHORTCUT_NAME, forceToken + context.getString(titleRes))
                    .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, iconRes))
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(context, Shortcut.class)
                            .putExtra(Shortcut.extraTag, shortcutAct)));
        else {
            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, "o_launcher_shortcut:" + shortcutAct)
                    .setShortLabel(forceToken + context.getString(titleRes))
                    .setIcon(Icon.createWithResource(context, iconRes))
                    .setIntent(new Intent(context, Shortcut.class)
                            .putExtra(Shortcut.extraTag, shortcutAct)
                            .setAction(Intent.ACTION_VIEW))
                    .build();
            new DebugLog("addLauncherShortcut: requestPinShortcut:"
                    + context.getSystemService(ShortcutManager.class).requestPinShortcut(shortcutInfo, null));
        }
    }

    /**
     * 取应用VersionName
     * （姑且放在这里）
     *
     * @param context c
     * @return vn
     */
    private static String getAppVersionName(Context context) {
        new DebugLog("getAppVersionName: " + context, DebugLog.LogLevel.I);
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0)
                return "";
        } catch (Exception e) {
            new DebugLog(e, "getAppVersionName", true);
        }
        return versionName;
    }

    /**
     * 将输入流转为字符串
     *
     * @param in     待转换的输入流
     * @param encode 字符编码
     * @return 转换后的字符串
     */
    private static String inputStream2String(InputStream in, @SuppressWarnings("SameParameterValue") String encode) {
        new DebugLog("inputStream2String", DebugLog.LogLevel.V);
        String str = "";
        try {
            if (encode == null || encode.equals(""))
                encode = "utf-8";
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, encode));
            StringBuilder sb = new StringBuilder();
            while ((str = reader.readLine()) != null)
                sb.append(str).append("\n");
            return sb.toString();
        } catch (Exception ignored) {
        }
        return str;
    }
}
