package com.rtm.frm.dianxin.utils;

import android.content.Context;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.rtm.common.model.RMLocation;
import com.rtm.frm.dianxin.R;
import com.rtm.frm.dianxin.bean.PrivateBuild;
import com.rtm.frm.dianxin.manager.AppContext;
import com.rtm.frm.map.MapView;
import com.rtm.frm.map.XunluMap;
import com.rtm.frm.model.BuildInfo;
import com.rtm.frm.model.CityInfo;
import com.rtm.frm.model.Floor;
import com.rtm.frm.model.Location;
import com.rtm.frm.model.POI;
import com.rtm.frm.model.RMBuildDetail;
import com.rtm.frm.model.RMBuildList;
import com.rtm.frm.model.RMCityList;
import com.rtm.frm.model.RMPois;
import com.rtm.frm.model.RMRoute;
import com.rtm.frm.utils.Handlerlist;
import com.rtm.frm.utils.RMBuildDetailUtil;
import com.rtm.frm.utils.RMBuildListUtil;
import com.rtm.frm.utils.RMCityListUtil;
import com.rtm.frm.utils.RMNavigationUtil;
import com.rtm.frm.utils.RMSearchPoiUtil;
import com.rtm.location.LocationApp;
import com.rtm.location.utils.RMLocationListener;

import java.util.ArrayList;
import java.util.List;

public class RMlbsUtils {

    public static final String KEY_SUPPORT_CITYS = "KEY_SUPPORT_CITYS";
    public static final String KEY_SUPPORT_PRIVATE_BUILDS = "KEY_SUPPORT_PRIVATE_BUILDS";

    // public static final String KEY_SUPPORT_BUILDS = "KEY_SUPPORT_BUILDS";

    public interface OnRmGetFinishListener {
        public void onGetCityListFinish(List<String> result);

        public void onGetBuildListFinish(List<BuildInfo> result);

        public void onGetBuildDetailFinish(BuildInfo result);

        public void onGetNavigationFinish(RMRoute result);

        /**
         * poi搜索
         *
         * @param result
         */
        public void onGetPoiSearchFinish(List<POI> result);

        public void onGetPrivateBuildFinish(List<PrivateBuild> result);
    }

    private static RMlbsUtils instance = null;

    public static RMlbsUtils getInstance() {
        if (instance == null) {
            instance = new RMlbsUtils();
            XunluMap.getInstance().init(AppContext.instance());// 初始化
//            ToastUtils.shortToast("map : " + RMVersionMap.VERSION);
        }
        return instance;
    }

    // 地图部分
    public void initMap(MapView mapView, Context context, Handler handler) {
        Handlerlist.getInstance().register(handler);
        mapView.initScale();
        mapView.startSensor();// 开启指针方向
    }

    // 定位部分
    public void initLocate(Context context, RMLocationListener listener) {
        LocationApp.getInstance().init(context);// 初始化定位
//        LocationApp.getInstance().setTestStatus(true);//定位使用测试地址
        LocationApp.getInstance().setLbsSign(LocationApp.OFFLINE);
        LocationApp.getInstance().setTestStatus(true);
        LocationApp.getInstance().setRootFolder("TestRtmap");
        LocationApp.getInstance().registerLocationListener(listener);
    }

    public void startLocate() {
        LocationApp.getInstance().start();// 开始定位
    }

    public void stopLocate() {
        LocationApp.getInstance().stop();// 开始定位
    }

    public void destroyLocate(RMLocationListener listener) {
        LocationApp.getInstance().unRegisterLocationListener(listener);
    }

    /**
     * 获取支持的城市
     *
     * @param listener
     */
    private static boolean getCityFinish = false;

    @SuppressWarnings("unchecked")
    public boolean getCityList(final OnRmGetFinishListener listener) {
        if (listener == null) {
            return false;
        }
        // 判断本地是否已经有数据
        getCityFinish = false;
        List<String> cacheCitys = ((List<String>)
                SharePrefUtil.getObj(AppContext.instance(),
                        KEY_SUPPORT_CITYS));
        if (cacheCitys != null && cacheCitys.size() != 0) {
            getCityFinish = true;
            listener.onGetCityListFinish(cacheCitys);
        }
        String key = XunluMap.getInstance().getApiKey();
        RMCityListUtil.requestCityList(XunluMap.getInstance().getApiKey(),
                new RMCityListUtil.OnGetCityListListener() {
                    @Override
                    public void onFinished(RMCityList result) {
                        //结果存储到本地
                        List<String> citys = result.getCitylist();
                        SharePrefUtil.saveObj(AppContext.instance(),
                                KEY_SUPPORT_CITYS, citys);

                        // 将结果回调
                        if (!getCityFinish) {
                            listener.onGetCityListFinish(citys);
                        }
                    }
                });
        return true;
    }

    private static boolean getBuildFinish = false;

    /**
     * 根据城市获取支持的建筑列表
     *
     * @param cityName
     * @param listener
     */
    @SuppressWarnings("unchecked")
    public boolean getBuildList(final String cityName,
                             final OnRmGetFinishListener listener) {
        if (listener == null) {
            return false;
        }
        // 判断本地是否已经有数据
        getBuildFinish = false;
        List<BuildInfo> cacheBuilds = ((List<BuildInfo>)
                SharePrefUtil.getObj(AppContext.instance(),
                        formatBuildsKey(cityName)));
        if (cacheBuilds != null && cacheBuilds.size() != 0) {
            getBuildFinish = true;
            listener.onGetBuildListFinish(cacheBuilds);
        }
        if (!NetWorkUtil.isNetworkConnected(AppContext.instance())) {
            ToastUtils.shortToast(R.string.network_not_connected);
            listener.onGetBuildListFinish(null);
            return false;
        }
        RMBuildListUtil.requestBuildList(XunluMap.getInstance().getApiKey(),
                cityName, new RMBuildListUtil.OnGetBuildListListener() {
                    @Override
                    public void onFinished(RMBuildList result) {
                        List<BuildInfo> buildInfos = null;
                        if (result.getError_code() == 0) {
                            buildInfos = new ArrayList<BuildInfo>();
                            List<CityInfo> cityInfos = result.getCitylist();
                            if (cityInfos != null && cityInfos.size() != 0) {
                                List<BuildInfo> builds = cityInfos.get(0).getBuildlist();
                                for (BuildInfo b : builds) {
                                    if (b.getLong() > 1 && b.getLat() > 1) {
                                        buildInfos.add(b);
                                    }
                                }
                            }
                            SharePrefUtil.saveObj(AppContext.instance(),
                                    formatBuildsKey(cityName), buildInfos);
                        } else {
                            ToastUtils.shortToast(R.string.load_empty);
                        }
                        if (!getBuildFinish) {
                            listener.onGetBuildListFinish(buildInfos);
                        }
                    }
                });
        return true;
    }

    private boolean getBuildDetailFinish = false;

    /**
     * *
     * 根据buildId获取build详情
     *
     * @param buildId
     * @param listener
     */
    public boolean getBuildDetail(final String buildId,
                               final OnRmGetFinishListener listener) {
    	
        if (listener == null) {
            return false;
        }
        // 判断本地是否已经有数据
        getBuildDetailFinish = false;
        BuildInfo cacheBuild = (BuildInfo)
                SharePrefUtil.getObj(AppContext.instance(), buildId);
        if (cacheBuild != null) {
            getBuildDetailFinish = true;
            listener.onGetBuildDetailFinish(cacheBuild);
        }


        RMBuildDetailUtil.requestBuildDetail(XunluMap.getInstance().getApiKey(),
                buildId, new RMBuildDetailUtil.OnGetBuildDetailListener() {

                    @Override
                    public void onFinished(RMBuildDetail detail) {
                        if (detail.getError_code() == 0) {
                            BuildInfo buildInfo = detail.getBuild();
                            if (buildInfo != null) {
                                SharePrefUtil.saveObj(AppContext.instance(), buildId,
                                        buildInfo);
                                SharePrefUtil.saveObj(AppContext.instance(),
                                        formatFloorsKey(buildId), buildInfo.getFloorlist());
                            }

                            if (!getBuildDetailFinish) {
                                listener.onGetBuildDetailFinish(buildInfo);
                            }
                        } else {
                            if (!getBuildDetailFinish) {
                                listener.onGetBuildDetailFinish(null);
                            }
                            ToastUtils.shortToast(R.string.load_empty);
                        }
                    }
                });
        return true;
    }

    /***
     * 根据buildId从本地获取楼层列表
     *
     * @param buildId
     * @return
     */
    public List<Floor> getLocFloorByBuildId(String buildId) {
        List<Floor> floors = (List<Floor>) SharePrefUtil.getObj(AppContext.instance(),
                formatFloorsKey(buildId));
        return floors;
    }

    /***
     * 建筑物内poi搜索
     *
     * @param buildId
     * @param keyWord
     * @param listener
     */
    public void searchPoi(final String buildId, String keyWord,
                          final OnRmGetFinishListener listener) {
        if (listener == null) {
            return;
        }
        RMSearchPoiUtil mSearchPoiUtil = new RMSearchPoiUtil();
        mSearchPoiUtil
                .setKey(XunluMap.getInstance().getApiKey())
                .setBuildid(buildId)
                .setKeywords(keyWord)
                .setOnSearchPoiListener(
                        new RMSearchPoiUtil.OnSearchPoiListener() {
                            @Override
                            public void onFinished(RMPois result) {
                                if (result.getError_code() == 0) {
                                    listener.onGetPoiSearchFinish(result.getPoilist());
                                } else {
                                    listener.onGetPoiSearchFinish(null);
                                }
                            }
                        }).searchPoi();
    }


    /***
     * 获取建筑内路线规划
     *
     * @param buildId
     * @param start
     * @param end
     * @param pois
     * @param listener
     */
    public void getNavigation(String buildId, POI start, POI end, ArrayList<POI> pois, final OnRmGetFinishListener listener) {
        RMNavigationUtil.requestNavigation(XunluMap.getInstance().getApiKey(), buildId, start, end, pois, new RMNavigationUtil.OnNavigationListener() {
            @Override
            public void onFinished(RMRoute rmRoute) {
                RMRoute route = null;
                if (rmRoute.getError_code() == 0) {
                    route = rmRoute;
                } else {
                    ToastUtils.shortToast(R.string.load_empty);
                }
                if (listener != null) {
                    listener.onGetNavigationFinish(route);
                }
            }
        });
    }

    private boolean getPrivateBuildsFinish = false;
    /***
     * 根据用户名密码，获取私有建筑列表
     *
     * @param userName
     * @param passWord
     * @param listener
     */
    public void getPrivateBuildList(String userName, String passWord, final OnRmGetFinishListener listener) {
        if (listener == null) {
            return;
        }
        String url = "http://api.rtmap.com:8081/rtmap/open2cur/login";
        HttpUtils httpUtils = new HttpUtils(8000);
        RequestParams params = new RequestParams();
        params.addBodyParameter("username", userName);
        params.addBodyParameter("password", passWord);

        // 判断本地是否已经有数据
        getPrivateBuildsFinish = false;
        List<PrivateBuild> cacheBuilds = ((List<PrivateBuild>)
                SharePrefUtil.getObj(AppContext.instance(),
                        KEY_SUPPORT_PRIVATE_BUILDS));
        if (cacheBuilds != null && cacheBuilds.size() != 0) {
            getPrivateBuildsFinish = true;
            listener.onGetPrivateBuildFinish(cacheBuilds);
        }

        httpUtils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                JSONObject reqObj = JSON.parseObject(responseInfo.result);
                List<PrivateBuild> privateBuilds = new ArrayList<PrivateBuild>();
                if ("1".equals(reqObj.getString("status") + "")) {
                    JSONArray array = reqObj.getJSONArray("results");
                    for (int i = 0; i < array.size(); ++i) {
                        PrivateBuild privateBuild = new PrivateBuild();
                        privateBuild.setBuildName(array.getJSONObject(i).getString("buildName"));
                        privateBuild.setFloor(array.getJSONObject(i).getJSONArray("floor"));
                        privateBuild.setBuildId(array.getJSONObject(i).getString("buildId"));
                        privateBuilds.add(privateBuild);
                    }
                    SharePrefUtil.saveObj(AppContext.instance(),
                            KEY_SUPPORT_PRIVATE_BUILDS, privateBuilds);
                }

                if (!getPrivateBuildsFinish) {
                    listener.onGetPrivateBuildFinish(privateBuilds);
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                listener.onGetPrivateBuildFinish(null);
            }
        });
    }

    /**
     * 将城市名格式化成本地存储的key
     *
     * @param cityName
     * @return
     */
    public String formatBuildsKey(String cityName) {
        return StringUtils.MD5(cityName + "_builds");
    }

    /**
     * 将buildId格式化成本地存储的key
     *
     * @param buildId
     * @return
     */
    public String formatFloorsKey(String buildId) {
        return buildId + "_floors";
    }

    public Location changeRmLocToLoc(RMLocation rmLocation) {
        String floor = getFloorById(rmLocation.getFloorID());
        Location location = new Location(rmLocation.getCoordX() / 1000,
                rmLocation.getCoordY() / 1000, floor, rmLocation.getBuildID());
        return location;
    }

    /**
     * 将楼层ID转换为对应的楼层F。。。
     *
     * @param floor
     * @return
     */
    public String getFloorById(int floor) {
        boolean isUp = ((floor / 10000 == 2) ? true : false);
        floor = floor % 10000;
        boolean isSharpFloor = (floor % 10 == 0);
        if (isUp) {
            if (isSharpFloor) {
                return String.format("F%d", floor / 10);
            } else {
                return String.format("F%.1f", floor / 10f);
            }
        } else {
            if (isSharpFloor) {
                return String.format("B%d", Math.abs(floor) / 10);
            } else {
                return String.format("B%.1f", Math.abs(floor) / 10f);
            }
        }
    }

    public void setNull() {
        instance = null;
    }

}
