package com.addx.ai.demo

object Global {
    var isLogin = false
    @JvmField
    var isSDKInited = false

    // 建议生成新的token
    // 格式是 key(userId) : value(token)
    var mTokenTests = HashMap<String,String>().apply {
        put("a4x-test1","Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6ImE0eC10ZXN0MSIsImFjY291bnRJZCI6InBhYXN0ZXN0Iiwic2VlZCI6IjEyZDVhODNiMmIzNjRlYmM5ZjFmZGExZTI1ZGRhODYzIiwiZXhwIjoyNjQyNjcyNTc4LCJ1c2VySWQiOjEwMDA3NzF9.S_XjOkNrU7hlTFIsYfz3Idm4o9E08Vi_Isz1TDnneBa6IElvJ5iBM5Laj3W3GHNFX_lSk9J1SVWo54aWGMCy9w")
        put("a4x-test2","Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6ImE0eC10ZXN0MiIsImFjY291bnRJZCI6InBhYXN0ZXN0Iiwic2VlZCI6IjlmMDJmOTBiN2JjMjQyNTA5YWZiOWQ3ODA4N2NiNzZjIiwiZXhwIjoyNjQyNjcyNjExLCJ1c2VySWQiOjEwMDA3NzJ9.SXcb0PKvuTSjcl2snQ0BJgRjZ2yO8lNKvmiyiIYVcPCiZp7DJ0cUCLsgl6QLnNU60KCYL7i170hb2hL_4xFUTA")
        put("a4x-test3","Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0aGlyZFVzZXJJZCI6ImE0eC10ZXN0MyIsImFjY291bnRJZCI6InBhYXN0ZXN0Iiwic2VlZCI6ImIxYmVkNzlhNWNjNzQxMGFiNjUwZjYyN2QwNDQyNTJhIiwiZXhwIjoyNjQyNjcyNjI5LCJ1c2VySWQiOjEwMDA3NzN9.AEY7zjZQ4NlznhxrSafXTOH1ziI8v3ZjpVZtUctwfkXIycVlRxFCIS-BNI21AyGZOOGX3aCyWQLXDrwKM8KfcQ")
    }

    internal interface Settings {
        companion object {
            /**
             * zendesk 配置项
             */
            //是否允许使用zendeskSDK ,允许就会使用zendesk原生网页，不允许，就会解析连接，使用网页。null 默认，false不允许，true 允许
            const val  enableZendesk: Boolean = false

            /**
             * 设置页面配置项
             */
            //是否隐藏设置时长的功能，true隐藏，false 不隐藏，默认不隐藏
            const val disableMotionPageSettings = false

            //是否使用更多按钮功能
            const val userMoreSettings = true
        }
    }
}