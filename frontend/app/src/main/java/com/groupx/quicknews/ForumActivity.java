package com.groupx.quicknews;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupx.quicknews.databinding.ActivityForumBinding;
import com.groupx.quicknews.helpers.HttpClient;
import com.groupx.quicknews.ui.forum.Comment;
import com.groupx.quicknews.ui.forum.CommentsViewAdapter;
import com.groupx.quicknews.ui.forumlist.Forum;
import com.groupx.quicknews.ui.forumlist.ForumsViewAdapter;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Response;

public class ForumActivity extends AppCompatActivity {

    private ActivityForumBinding binding;
    private RecyclerView forumView;
    private EditText commentText;
    private Button postButton;
    private String forumID;
    private List<Comment> comments;
    final static String TAG = "ForumActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityForumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        forumID = getIntent().getStringExtra("forumID");

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getIntent().getStringExtra("forumName"));

        forumView = findViewById(R.id.view_comment);
        commentText = findViewById(R.id.edit_post);

        Log.d(TAG, LoginActivity.getAccount().getDisplayName());

        postButton = findViewById(R.id.button_post);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postComment(commentText.getText().toString());
                commentText.getText().clear();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getComments();
    }

    private void getComments () {
        String url = getString(R.string.server_dns) + "forums/" + forumID;
        try {
        HttpClient.getRequest(url, new HttpClient.ApiCallback(){
            @Override
            public void onResponse(Response response) throws IOException {
                int statusCode = response.code();
                if (statusCode == 200){
                    String responseBody = response.body().string();
                    Log.d(TAG, responseBody);

                    //update forums view
                    ObjectMapper mapper = new ObjectMapper();
                    comments = Arrays.asList(mapper.readValue(responseBody, Comment[].class));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            forumView.setLayoutManager(new LinearLayoutManager(ForumActivity.this));
                            forumView.setAdapter(new CommentsViewAdapter(getApplicationContext(), comments));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "exception", e);
            }
        });
        }
        catch(Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

    private void postComment(String comment) {
        Log.d(TAG,comment);
        String url = getString(R.string.server_dns) + "addComment/" + forumID;
        try {
            JSONObject json = new JSONObject();
            json.put("userId", LoginActivity.getAccount().getIdToken());
            json.put("commentData", comment);
            HttpClient.postRequest(url, json.toString(), new HttpClient.ApiCallback(){
                @Override
                public void onResponse(Response response) throws IOException{

                    String responseBody = response.body().string();
                    int statusCode = response.code();

                    Log.d(TAG, responseBody);
                    Log.d(TAG, String.valueOf(statusCode));
                    if (statusCode == 200){

                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "exception", e);
                }
            });
        }
        catch(Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

    //https://dev.to/ahmmedrejowan/hide-the-soft-keyboard-and-remove-focus-from-edittext-in-android-ehp
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}