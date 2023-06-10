package com.example.wishwash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPOIItem.ImageOffset;
import net.daum.mf.map.api.MapPOIItem.MarkerType;
import net.daum.mf.map.api.MapPOIItem.ShowAnimationType;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder.ReverseGeoCodingResultListener;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapView.CurrentLocationEventListener;
import net.daum.mf.map.api.MapView.MapViewEventListener;
import net.daum.mf.map.api.MapView.POIItemEventListener;

public abstract class MapActivity extends AppCompatActivity implements CurrentLocationEventListener, MapViewEventListener, ReverseGeoCodingResultListener, POIItemEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapView mapView = new MapView(this);
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        mapView.setCurrentLocationEventListener(this);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);

        // 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            startLocationService();
        }
    }

    private void startLocationService() {
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracy) {
        // 현재 위치가 업데이트될 때 호출됩니다.
        // 주변 세탁소를 검색하고 마커를 추가하는 기능을 여기에 추가하세요.
        double latitude = currentLocation.getMapPointGeoCoord().latitude;
        double longitude = currentLocation.getMapPointGeoCoord().longitude;

        // 현재 위치에 마커 추가
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("현재 위치");
        marker.setTag(0);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
        marker.setMarkerType(MarkerType.CustomImage);
        marker.setCustomImageResourceId(R.drawable.marker_current_location);
        marker.setCustomImageAutoscale(false);
        marker.setCustomImageAnchorPointOffset(new ImageOffset(30, 30));
        marker.setCustomImageAnchor(0.5f,1.0f);
        marker.setShowAnimationType(ShowAnimationType.SpringFromGround);
        marker.setDraggable(false);

        mapView.addPOIItem(marker);

        // 주변 세탁소 검색 및 마커 표시
        LocalSearch localSearch = new LocalSearch();
        localSearch.setKeyword("세탁소"); // 검색 키워드 설정
        localSearch.setLocation(latitude, longitude); // 검색 중심 위치 설정
        localSearch.setRadius(1000); // 검색 반경 설정 (미터 단위)
        localSearch.setPage(1); // 검색 결과 페이지 설정
        localSearch.setListener(new LocalSearchResponseListener() {
            @Override
            public void onSuccess(LocalMapPOIItem[] poiItems) {
                // 세탁소 검색 성공 시 호출되는 콜백 메서드
                for (LocalMapPOIItem poiItem : poiItems) {
                    // 각 세탁소에 대한 정보를 가져와서 마커를 생성하고 추가합니다.
                    MapPOIItem marker = new MapPOIItem();
                    marker.setItemName(poiItem.getItemName());
                    marker.setTag(poiItem.getTag());
                    marker.setMapPoint(poiItem.getMapPoint());
                    marker.setMarkerType(MarkerType.BluePin); // 마커 아이콘 유형 설정
                    marker.setSelectedMarkerType(MarkerType.RedPin); // 선택된 마커 아이콘 유형 설정
                    mapView.addPOIItem(marker);
                }
            }

            @Override
            public void onFailure(int errorCode) {
                // 세탁소 검색 실패 시 호출되는 콜백 메서드
                Toast.makeText(MapActivity.this, "세탁소 검색에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        localSearch.searchPOIAsyn();
    }

    private class LocalSearch {
        public void searchPOIAsyn() {
        }

        public void setKeyword(String 세탁소) {
        }

        public void setLocation(double latitude, double longitude) {
        }

        public void setRadius(int i) {
        }

        public void setPage(int i) {
        }

        public void setListener(LocalSearchResponseListener localSearchResponseListener) {
        }
    }

    private abstract class LocalSearchResponseListener {
        public abstract void onSuccess(LocalMapPOIItem[] poiItems);

        public abstract void onFailure(int errorCode);
    }

    private class LocalMapPOIItem {
        public int getTag() {
            return 0;
        }

        public String getItemName() {
            return null;
        }

        public MapPoint getMapPoint() {
            return null;
        }
    }

    private class CustomImageAnchorPoint {
        public CustomImageAnchorPoint(float v, float v1) {
        }
    }
}