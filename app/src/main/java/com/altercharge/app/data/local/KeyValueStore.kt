/** 기기 저장소. 테스트에서 메모리로 바꿔 끼운다 (SharedPreferences는 OfflineSnapshotStore.kt) */
package com.altercharge.app.data.local

interface KeyValueStore {
    fun getString(key: String): String?
    fun putStrings(values: Map<String, String?>)
}
