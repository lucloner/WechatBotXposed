package net.vicp.biggee.xposed.wechat

import de.robv.android.xposed.XposedBridge

object ErrorHandler : Thread.UncaughtExceptionHandler {
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
        val stringBuilder = StringBuilder()
        if (t != null) {
            stringBuilder.append("${t.name}\n")
        }
        if (e != null) {
            stringBuilder.append("${e.localizedMessage}\n")
            e.stackTrace.forEach {
                stringBuilder.append("${it.isNativeMethod} ${it.fileName} ${it.className} ${it.methodName} ${it.lineNumber}\n")
            }
        }
        XposedBridge.log("Aichat $stringBuilder")
        e?.printStackTrace()
    }

}