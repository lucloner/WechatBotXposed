package net.vicp.biggee.xposed.wechat

import de.robv.android.xposed.XposedBridge

class ProcessException : Thread.UncaughtExceptionHandler {
    /**
     * Method invoked when the given thread terminates due to the
     * given uncaught exception.
     *
     * Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     * @param t the thread
     * @param e the exception
     */
    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.printStackTrace()
        XposedBridge.log("Hello Wechat!${e?.localizedMessage}")
//        XposedBridge.log("Hello Wechat!${e?.stackTrace?.forEach { "${it.isNativeMethod}${it.fileName}${it.className}${it.methodName}${it.lineNumber}\n" }}")
    }
}