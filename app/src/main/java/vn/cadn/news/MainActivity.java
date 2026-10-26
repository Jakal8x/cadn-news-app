package vn.cadn.news;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private final List<Category> categories = new ArrayList<>();
    private NewsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private int currentCategoryIndex = 0;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCategories();

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        for (Category cat : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(cat.getName()));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentCategoryIndex = tab.getPosition();
                loadNews(categories.get(currentCategoryIndex).getRssUrl());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                loadNews(categories.get(currentCategoryIndex).getRssUrl());
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this);
        recyclerView.setAdapter(adapter);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        swipeRefresh.setOnRefreshListener(() -> {
            loadNews(categories.get(currentCategoryIndex).getRssUrl());
        });

        // Initial load
        loadNews(categories.get(0).getRssUrl());
    }

    private void initCategories() {
        categories.add(new Category("Tin mới nhất", "https://cadn.com.vn/tin-moi-nhat.rss"));
        categories.add(new Category("Thời sự", "https://cadn.com.vn/rss/thoi-su-1.rss"));
        categories.add(new Category("An ninh - Trật tự", "https://cadn.com.vn/rss/an-ninh-trat-tu-78.rss"));
        categories.add(new Category("Công an nhân dân", "https://cadn.com.vn/rss/cong-an-nhan-dan-5.rss"));
        categories.add(new Category("Pháp luật", "https://cadn.com.vn/rss/phap-luat-6.rss"));
        categories.add(new Category("Kinh tế", "https://cadn.com.vn/rss/kinh-te-12.rss"));
        categories.add(new Category("Xã hội", "https://cadn.com.vn/rss/xa-hoi-13.rss"));
        categories.add(new Category("Quốc tế", "https://cadn.com.vn/rss/quoc-te-19.rss"));
        categories.add(new Category("Thể thao", "https://cadn.com.vn/rss/the-thao-25.rss"));
        categories.add(new Category("Công nghệ số", "https://cadn.com.vn/rss/cong-nghe-so-116.rss"));
    }

    private void loadNews(String rssUrl) {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        tvEmptyState.setVisibility(View.GONE);

        executorService.execute(() -> {
            HttpURLConnection conn = null;
            InputStream stream = null;
            try {
                URL url = new URL(rssUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(15000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = conn.getInputStream();
                    List<Article> articles = RssParser.parse(stream);
                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        adapter.setArticles(articles);
                        if (articles.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                            tvEmptyState.setText("Không có tin tức nào.");
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                        }
                    });
                } else {
                    notifyError("Mã phản hồi: " + conn.getResponseCode());
                }
            } catch (Exception e) {
                notifyError(e.getMessage());
            } finally {
                try {
                    if (stream != null) stream.close();
                } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void notifyError(String err) {
        mainHandler.post(() -> {
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("Không thể tải tin tức. Vui lòng kiểm tra kết nối mạng.");
            Toast.makeText(MainActivity.this, "Lỗi kết nối: " + err, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
