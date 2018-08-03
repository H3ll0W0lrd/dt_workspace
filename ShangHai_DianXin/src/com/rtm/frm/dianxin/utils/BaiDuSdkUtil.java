package com.rtm.frm.dianxin.utils;

import android.view.View;
import android.view.ViewStub;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.model.BuildInfo;

import java.util.List;

/**
 * Created by ly on 15-7-31.
 */
public class BaiDuSdkUtil {
    public interface OnMarkerDetailClickListerner {
        public void onMarkerDetailClickListener(Marker marker);
    }

    private static boolean isChangeBdLatLng = false;

    /***
     * 谷歌经纬度转换为百度经纬度
     *
     * @param lat
     * @param lng
     * @return
     */
    public static LatLng changeToBdLatLng(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        if (isChangeBdLatLng) {
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.COMMON);
            latLng = converter.coord(latLng).convert();
        }
        return latLng;
    }

    /***
     * 批量转换谷歌经纬度转换为百度经纬度
     *
     * @param buildInfos
     * @return
     */
    public static List<BuildInfo> changeToBdLatLngBatch(List<BuildInfo> buildInfos) {
        if (isChangeBdLatLng) {
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.COMMON);
            for (BuildInfo build : buildInfos) {
                double lat = build.getLat();
                double lng = build.getLong();
                LatLng latLng = new LatLng(lat, lng);
                latLng = converter.coord(latLng).convert();
                build.setLatLong((float) latLng.latitude, (float) latLng.longitude);
            }
        }
        return buildInfos;
    }

    private static View mMarkerDetailView;
    private static TranslateAnimation anim;
    private static View stepInto;

    /***
     * 显示marker详情
     *
     * @param viewStub
     * @param marker
     * @param myLocationData
     */
    public static void showMarkerDetail(final ViewStub viewStub, final Marker marker, MyLocationData myLocationData, final OnMarkerDetailClickListerner clickListerner) {

        if (viewStub.getLayoutResource() == 0) {
            viewStub.setLayoutResource(R.layout.bd_map_marker_detail);
            mMarkerDetailView = viewStub.inflate();
            stepInto = mMarkerDetailView.findViewById(R.id.tv_step_into);
            anim = new TranslateAnimation(stepInto.getWidth(),
                    stepInto.getWidth() + 10, stepInto.getHeight(), stepInto.getHeight());

// 利用 CycleInterpolator 参数 为float 的数  表示 抖动的次数，而抖动的快慢是由 duration 和 CycleInterpolator 的参数的大小 联合确定的
            anim.setInterpolator(new CycleInterpolator(3f));
            anim.setDuration(800);
        }
        viewStub.setVisibility(View.VISIBLE);
        TextView buildName = (TextView) mMarkerDetailView.findViewById(R.id.tv_build_name);
        TextView buildDis = (TextView) mMarkerDetailView.findViewById(R.id.tv_build_dis);
        String name = marker.getTitle();
        buildName.setText(name + "");
        if (myLocationData == null) {
            buildDis.setText("");
        } else {
            double dis = DistanceUtil.getDistance(marker.getPosition(), new LatLng(myLocationData.latitude, myLocationData.longitude));//单位为米
            dis = dis / 1000;
            if (dis < 1) {
                buildDis.setText("<1km");
            } else {
                int i = (int) dis * 100;
                dis = i / 100d;
                buildDis.setText(dis + "km");
            }
        }
        if (clickListerner != null) {
            mMarkerDetailView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListerner.onMarkerDetailClickListener(marker);
                }
            });
        }

        stepInto.startAnimation(anim);

    }

    public static void hideMarkerDetail(ViewStub stub) {
        if (mMarkerDetailView != null) {
            View stepInto = mMarkerDetailView.findViewById(R.id.tv_step_into);
            if (stepInto.getAnimation() != null) {
                stepInto.getAnimation().cancel();
            }
        }
        stub.setVisibility(View.GONE);
    }

}
