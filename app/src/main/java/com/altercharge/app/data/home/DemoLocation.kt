/**
 * 1차 시연용 현재 위치(서울 종로구 평창동, 아파트 단지 근처). 2차에 FusedLocation으로 바꾼다 (README 알려진 한계).
 * 도심 한가운데는 충전소가 100m 안에 몰려 거리가 전부 0.1km로 보여서, 주변 충전소가 0.6 ~ 1.1km로 퍼진 주거 지역으로 잡았다
 */
package com.altercharge.app.data.home

val DEMO_LOCATION: LatLng = LatLng(37.6138, 126.9839)
