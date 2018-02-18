package com.ryuunoakaihitomi.rebootmenu.util;

import android.util.Log;

/**
 * 日志输出调试工具
 * Created by ZQY on 2018/2/11.
 *
 * @author ZQY
 * @version 1.0
 * @see android.util.Log
 */

public class DebugLog {

    //标签
    private final String TAG = "rebootmenu";
    //切记 ：调试输出开关
    //算了算了，源代码都被看透了还想办法管束调试日志的输出...
    private final boolean isLog = true;
    //日志等级
    public static final int V = 0;
    public static final int D = 1;
    public static final int I = 2;
    public static final int W = 3;
    public static final int E = 4;
    public static final int WTF = 5;

    /**
     * debug级日志输出
     *
     * @param msg 输出内容
     */
    public DebugLog(String msg) {
        if (isLog)
            Log.d(TAG, msg);
    }

    /**
     * 其他等级日志输出
     *
     * @param msg 输出内容
     * @param lev 输出等级
     */
    public DebugLog(String msg, int lev) {
        if (isLog)
            switch (lev) {
                case V:
                    Log.v(TAG, msg);
                    break;
                case D:
                    Log.d(TAG, msg);
                    break;
                case I:
                    Log.i(TAG, msg);
                    break;
                case W:
                    Log.w(TAG, msg);
                    break;
                case E:
                    Log.e(TAG, msg);
                    break;
                case WTF:
                    Log.wtf(TAG, msg);
                    break;
                default:
                    Log.w(TAG, "(level?)" + msg);
            }
    }
}
