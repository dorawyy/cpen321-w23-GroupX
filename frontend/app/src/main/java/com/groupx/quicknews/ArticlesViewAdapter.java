package com.groupx.quicknews;

import static android.app.PendingIntent.getActivity;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArticlesViewAdapter extends RecyclerView.Adapter<ArticleViewHolder> {
    private Context context;
    private List<Article> articles;
    final static String TAG = "ArticleView";
    public ArticlesViewAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ArticleViewHolder holder = new ArticleViewHolder(LayoutInflater.from(context).inflate(R.layout.article_view, parent, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, holder.articleTitle.getText().toString());

                if (holder.hiddenView.getVisibility() == View.VISIBLE) {
                    // The transition of the hiddenView is carried out by the TransitionManager class.
                    // Here we use an object of the AutoTransition Class to create a default transition
                    TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                    holder.hiddenView.setVisibility(View.GONE);
                }
                // If the CardView is not expanded, set its visibility to
                // visible and change the expand more icon to expand less.
                else {
                    TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
                    holder.hiddenView.setVisibility(View.VISIBLE);
                }
            }
        });

        holder.redirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(articles.get(holder.getAdapterPosition()).getUrl()); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.articleTitle.setText(articles.get(position).getTitle());
        holder.articleSynopsis.setText(articles.get(position).getSynopsis());
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
}

