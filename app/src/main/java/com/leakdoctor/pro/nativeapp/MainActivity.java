package com.leakdoctor.pro.nativeapp;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private PermissionRequest pending;
    private static final String HOME = "https://rlgnsekf-ux.github.io/leakdoctor-pro/";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (!request.getOrigin().toString().startsWith(HOME)) { request.deny(); return; }
                    boolean camera = false;
                    for (String resource : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) camera = true;
                    }
                    if (!camera) { request.deny(); return; }
                    pending = request;
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) approveCamera();
                    else requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
                });
            }
        });
        web.loadUrl(HOME);
    }
    private void approveCamera() {
        if (pending != null) { pending.grant(new String[]{PermissionRequest.RESOURCE_VIDEO_CAPTURE}); pending = null; }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 101 && pending != null) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) approveCamera();
            else { pending.deny(); pending = null; }
        }
    }
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
