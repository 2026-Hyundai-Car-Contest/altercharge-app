/** 서버 응답 · 기기 저장값의 enum 이름을 읽는다. 모르는 이름이면 null: 값이 늘거나 깨져도 앱이 죽지 않게 */
package com.altercharge.app.data

internal inline fun <reified E : Enum<E>> enumOrNull(name: String): E? = enumValues<E>().firstOrNull { it.name == name }
