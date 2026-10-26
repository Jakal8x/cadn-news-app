package vn.cadn.news;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.ByteArrayInputStream;

public class DetailActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Article article;

    // Danh sách domain máy chủ quảng cáo cần chặn
    private static final String[] AD_HOSTS = new String[] {
        "googleads", "pagead2", "googlesyndication", "doubleclick",
        "adservice", "eclick", "adnxs", "criteo", "pubmatic",
        "scorecardresearch", "facebook.net", "connect.facebook.net",
        "googletagservices", "smartadserver", "ambientdigitalgroup"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        article = (Article) getIntent().getSerializableExtra("article");

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (article != null) {
                getSupportActionBar().setTitle(article.getTitle());
            }
        }

        progressBar = findViewById(R.id.detailProgressBar);
        webView = findViewById(R.id.webView);

        setupWebView();

        if (article != null && !article.getLink().isEmpty()) {
            webView.loadUrl(article.getLink());
        }
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            // Chặn tải script và ảnh quảng cáo từ cấp độ gói tin mạng
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                for (String adHost : AD_HOSTS) {
                    if (url.contains(adHost)) {
                        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("cadn.com.vn")) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                injectAdBlockAndCleanMode(view);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                injectAdBlockAndCleanMode(view);
            }
        });
    }

    private void injectAdBlockAndCleanMode(WebView view) {
        // Loại bỏ triệt để banner quảng cáo, header, footer và định dạng phông chữ chuẩn
        String css =
            ".ad-unit, .adsbygoogle, ins.adsbygoogle, [class*='adv'], [class*='banner'], " +
            "[id*='google_ads'], [id*='banner'], [class*='sticky-ads'], [class*='floating-ads'], " +
            "[class*='popup'], [class*='cadn-modal'], [class*='cadn-login'], .cadn-control-share, " +
            ".fb-login-button, .g-signin2, .icon-adv, header, footer, nav, .cadn-header, " +
            ".cadn-footer, .info-ft, .info-link-ft, .cadn-menu-cate " +
            "{ display: none !important; visibility: hidden !important; height: 0 !important; max-height: 0 !important; overflow: hidden !important; margin: 0 !important; padding: 0 !important; } " +
            "body { padding: 14px 16px !important; margin: 0 !important; font-size: 18px !important; line-height: 1.7 !important; color: #212121 !important; background-color: #FAFAFA !important; } " +
            "img { max-width: 100% !important; height: auto !important; border-radius: 8px !important; display: block !important; margin: 12px auto !important; }";

        String js =
            "javascript:(function() {" +
            "  var style = document.getElementById('cadn-clean-style');" +
            "  if (!style) {" +
            "    style = document.createElement('style');" +
            "    style.id = 'cadn-clean-style';" +
            "    style.type = 'text/css';" +
            "    style.innerHTML = '" + css.replace("'", "\\'") + "';" +
            "    (document.head || document.documentElement).appendChild(style);" +
            "  }" +
            "  function removeAds() {" +
            "    var selectors = ['.ad-unit', '.adsbygoogle', 'ins.adsbygoogle', '[class*=\"adv\"]', '[class*=\"banner\"]', '[id*=\"google_ads\"]', 'header', 'footer', 'nav', '.info-ft', '.info-link-ft', '.cadn-login', '.cadn-modal'];" +
            "    selectors.forEach(function(sel) {" +
            "      var els = document.querySelectorAll(sel);" +
            "      for (var i = 0; i < els.length; i++) { els[i].remove(); }" +
            "    });" +
            "  }" +
            "  removeAds();" +
            "  setInterval(removeAds, 700);" +
            "})()";

        view.evaluateJavascript(js, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            shareArticle();
            return true;
        } else if (id == R.id.action_browser) {
            openInBrowser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareArticle() {
        if (article == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.getTitle() + "\n" + article.getLink());
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết qua:"));
    }

    private void openInBrowser() {
        if (article == null || article.getLink().isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getLink()));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
