package com.example.safeentry;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private final static String TAG = "MainActivity";
    private final static String url = "https://www.safeentry.gov.sg/logins/new_clicker_login";
    private final static int REQUEST_CODE = 100;

    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;

    private String[] permissions = {Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EasyPermissions.requestPermissions(this, "Request Permissions", REQUEST_CODE, permissions);

        initView();
    }

    //初始化View
    private void initView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                webView.reload();
            }
        });
        // 完成了上述功能后，会发现网页无法上拉，往上滑动就会触发下拉刷新控件的refresh事件
        // 说明：canChildScrollUp方法返回的true/false表示子视图是否可返回顶部，我们改成webView.getScrollY() > 0后表示webView到顶部时返回false，refreshLayout可接收到下拉动作，触发refresh事件
        // 1. 在顶部时，返回false，表示子视图不可滚动，refreshLayout接收到滑动事件，引出滑动视图和调用滑动刷新方法
        // 2. 不在顶部时，webView.getScrollY() > 0，返回true，表示子视图可滚动，refreshLayout中canChildScrollUp()返回true，刷新控件不再处理滑动问题，所以没有调用滑动刷新方法
        // 设置子视图是否允许滚动到顶部
        swipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@Nullable SwipeRefreshLayout parent, @Nullable View child) {
                return webView.getScrollY() > 0;
            }
        });

        //从布局文件中扩展webView
        webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            // Grant permissions for cam
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                request.grant(request.getResources());
            }
        });
        initWebViewSetting();
    }

    //初始化webViewSetting
    private void initWebViewSetting() {
        WebSettings settings = webView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        // 是否可访问Content Provider的资源，默认值 true
        settings.setAllowContentAccess(true);
        // 是否可访问本地文件，默认值 true
        settings.setAllowFileAccess(true);
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        settings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        settings.setAllowUniversalAccessFromFileURLs(false);
        //开启JavaScript支持
        settings.setJavaScriptEnabled(true);
        // 支持缩放
        settings.setSupportZoom(true);
        //加载地址
        webView.loadUrl(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied
        // ...
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //如果按下的是回退键且历史记录里确实还有页面
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
