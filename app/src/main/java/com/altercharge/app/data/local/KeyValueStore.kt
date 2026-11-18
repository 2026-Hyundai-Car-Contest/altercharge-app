/** 기기 저장소. 테스트에서 메모리로 바꿔 끼운다 */
package com.altercharge.app.data.local

import android.content.Context

interface KeyValueStore {
    fun getString(key: String): String?

    /** 여러 값을 한 번에 쓴다. 값이 null이면 그 키를 지운다 */
    fun putStrings(values: Map<String, String?>)
}

class SharedPreferencesStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putStrings(values: Map<String, String?>) {
        prefs.edit().apply {
            values.forEach { (key, value) -> if (value == null) remove(key) else putString(key, value) }
        }.apply()
    }

    private companion object {
        const val PREFS_NAME = "altercharge_offline"
    }
}
