package library.com.webviewapplication;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView mWebView;
    private TextView tv;
    private TextView javaCallJsTv;
    private TextView jsCallJavaTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setttingWebView();
    }

    /***
     * 设置webView页面
     */
    private void setttingWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 启用JavaScript
        webSettings.setUserAgentString("elong+1099093");
        //允许弹出框
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        //  支持 web chrome://inspect调试
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
//        mWebView.loadUrl("file:///android_asset/js.html");
        String url = "http://m.elong.com";
        mWebView.loadUrl(url);
        addCookies(url, Calendar.getInstance().toString(), "fadadfasfdf");
        readCookies(url);
        // 原生方式，注册交互的类
        mWebView.addJavascriptInterface(new JsToJava(), "prices");

        // Js的Prompt实际上就是一个确定弹出框，Android上一般用不上这个功能
        // 我们直接把弹出框这个功能拿来用做交互，当需要交互的时候，就把交互参数作为弹出框内容，然后在Android中拦截了就行了
        // 如果你对HTML不熟悉，就告诉你们的前段：就是js的prompt方法，然后把上面的html给他看看他肯定就知道怎么写了
        // 这种方式我个人不太喜欢，毕竟把JsPromot给占用了，心里不爽...
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                // 这里拿到参数就可以根据约定好的格式解析了
                tv.setText("prompt方式，参数：" + message);
                // 调用一下cancel或者confirm都行，就是模拟手动点击了确定或者取消按钮
                result.cancel();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                tv.setText("Confirm方式，参数：" + message);
                // 调用一下cancel或者confirm都行，就是模拟手动点击了确定或者取消按钮
                result.cancel();
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 超链接的方式
                // WebView中任何跳转都会走这个方法，我们在这里进行判断，如果是我们约定好的连接，就进行自己的操作，否则就放行
                tv.setText("url方式交互，url是：" + url);
                return true; // 拦截了，如果不拦截就是 view.loadUrl(url)
            }
        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.java_js) {
            mWebView.loadUrl("javascript:myfuncion(1)");
        } else if (id == R.id.js_java) {

        }
    }

    // 原生的方式
    private class JsToJava {
        // 高版本需要加这个注解才能生效
        @JavascriptInterface
        public void jsMethod(final String paramFromJS) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv.setText("传统方式js调用java，参数：" + paramFromJS);
                }
            });
        }
    }

    /***
     *  初始化 webView 页面
     */
    private void initView() {
        mWebView = (WebView) findViewById(R.id.my_webview);
        javaCallJsTv = (TextView) findViewById(R.id.java_js);
        jsCallJavaTv = (TextView) findViewById(R.id.js_java);
        tv = (TextView) findViewById(R.id.webview_tv);
        initEvents();
    }

    private void initEvents() {
        javaCallJsTv.setOnClickListener(this);
        jsCallJavaTv.setOnClickListener(this);
    }

    /**
     * 读写cookie
     *
     * @param url
     * @param expiresDate
     * @param ticket
     */
    private void addCookies(String url, String expiresDate, String ticket) {
        try {
            CookieManager cookieManager = CookieManager.getInstance();

            cookieManager.setAcceptCookie(true);
            // 设置跨域cookie读取
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.WEEK_OF_MONTH, +1);
            cookieManager.setCookie(url, "sa_auth=" + ticket + ";" + "expires=" + calendar.getTime().toString() + ";" + "domain=" + ".elong.com" + ";" + "Path=/" + ";");
            cookieManager.setCookie(url, "sa_auth=" + "fdafdsffdfdfdfsfa" + ";"  + "domain=" + "m.elong.com" + ";" +"dfafsdf="+"dasfasdf"+";");
            if (Build.VERSION.SDK_INT < 21) {
                CookieSyncManager.getInstance().sync();
            } else {
                CookieManager.getInstance().flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读写cookie
     *
     * @param url
     */
    private void readCookies(String url) {
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookieStr = cookieManager.getCookie(".elong.com");

            tv.setText(cookieStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
