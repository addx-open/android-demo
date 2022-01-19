package com.addx.ai.demo;

public class Global {
    public static boolean isLogin = false;
    public static boolean isSDKInited = false;


    interface Settings {
        /**
         * zendesk 配置项
         */
        //是否允许使用zendeskSDK ,允许就会使用zendesk原生网页，不允许，就会解析连接，使用网页。null 默认，false不允许，true 允许
        public static Boolean enableZendesk = null;


        /**
         * 设置页面配置项
         */
        //是否隐藏设置时长的功能，true隐藏，false 不隐藏，默认不隐藏
        public static boolean disableMotionPageSettings = false;
        //是否使用更多按钮功能
        public static boolean userMoreSettings = true;
    }
}
