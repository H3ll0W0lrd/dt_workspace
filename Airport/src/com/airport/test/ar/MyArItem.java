package com.airport.test.ar;

import android.widget.TextView;

import com.airport.test.R;

import java.text.NumberFormat;

/**
 * Created by liyan on 15/11/17.
 */
public class MyArItem extends ArShowView {

    @Override
    public void setTargetName(String targetName) {
        super.setTargetName(targetName);
        ((TextView) getLayoutView().findViewById(R.id.tv_item_name)).setText(targetName);
    }

    @Override
    public void setDistance(float distance) {
        NumberFormat ddf1=NumberFormat.getNumberInstance() ;


        ddf1.setMaximumFractionDigits(2);
        String s= ddf1.format(distance) ;
//        ((TextView) getLayoutView().findViewById(R.id.tv_item_dis)).setText("dis:" + s+"米");
    }
}
