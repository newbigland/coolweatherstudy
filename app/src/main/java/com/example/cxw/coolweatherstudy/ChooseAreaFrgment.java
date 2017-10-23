package com.example.cxw.coolweatherstudy;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cxw.coolweatherstudy.db.City;
import com.example.cxw.coolweatherstudy.db.County;
import com.example.cxw.coolweatherstudy.db.Province;
import com.example.cxw.coolweatherstudy.util.HttpUtil;
import com.example.cxw.coolweatherstudy.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * Created by cxw on 2017/10/23.
 */

public class ChooseAreaFrgment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVLE_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currrentLevel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area, container, false); //通过碎片布局文件生成碎片的视图对象
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter); //将适配器安装在listView控件上
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //给listView和backButton设置点击监听器，只有点击了子项或按钮才会执行各自的回调方法
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currrentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currrentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currrentLevel == LEVLE_COUNTY) {
                    queryCities();
                } else if (currrentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });

        queryProvinces(); //默认执行查询省数据
    }

    //查询所有省，先从数据库查，没有查到再从服务器上查
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);//通过LitePal从数据库sqlite中查询全部省份
        if (provinceList.size() > 0) { //数据库有数据时
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName()); //dataList存放省名
            }
            adapter.notifyDataSetChanged(); //数据变化了，适配器通知视图自我刷新
            listView.setSelection(0); //选择第一个子项
            currrentLevel = LEVEL_PROVINCE;
        } else { //数据库没数据时
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    //查询选中省的所有市，先从数据库查，没有时再从服务器上查
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //从数据库sqlite中查询选中省份的所有市
        cityList = DataSupport.where("provinceid = ? ", String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0) { //从数据库查
            dataList.clear(); //清空传给适配器的列表中原来的数据
            for (City city : cityList) {
                dataList.add(city.getCityName());  //dataList存放选中省的市名
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currrentLevel = LEVEL_CITY;
        } else { //数据库没数据库时从服务器上查
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    //查询选中的市下面的所有县，先充数据库查，没有时再从服务器上查
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ? ", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currrentLevel = LEVLE_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");

        }
    }


    //根据传入的API地址和类型在服务器上查询省、市、县数据
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        //发送网络请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //响应成功，会回调此方法
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resonseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(resonseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(resonseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(resonseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() { //网络请求在子线程中执行，切换到主线程更新UI
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces(); //重新加载省数据
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });

    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss(); //从界面上删除进度对话框
        }
    }
}
