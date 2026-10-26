package vn.cadn.news;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private final Context context;
    private final List<Article> articleList = new ArrayList<>();

    public NewsAdapter(Context context) {
        this.context = context;
    }

    public void setArticles(List<Article> articles) {
        this.articleList.clear();
        if (articles != null) {
            this.articleList.addAll(articles);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.tvTitle.setText(article.getTitle());
        holder.tvDate.setText(article.getPubDate());
        holder.tvDesc.setText(article.getDescription());

        String imgUrl = article.getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            holder.ivThumb.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(imgUrl, holder.ivThumb, R.drawable.ic_placeholder);
        } else {
            holder.ivThumb.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("article", article);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumb;
        final TextView tvTitle;
        final TextView tvDate;
        final TextView tvDesc;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }
}
