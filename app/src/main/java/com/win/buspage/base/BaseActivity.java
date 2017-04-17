package com.win.buspage.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Author: wangshuang
 * Time: 2017/4/17
 * Email:xiaoshuang990@sina.com
 */

public abstract class BaseActivity  extends AppCompatActivity{

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(getView());
        unbinder= ButterKnife.bind(this);
        initData();
    }

    public abstract  int getView();

    public abstract  void initData();
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
