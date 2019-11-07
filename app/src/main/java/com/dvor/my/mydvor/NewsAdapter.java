package com.dvor.my.mydvor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    private LayoutInflater inflater;
    private int layout;
    private List<News> news;
    String organizationId;
    private List<String> likes;
    private List<String> dislikes;
    private FirebaseAuth mAuth;

    enum Comment {
        absent, like, dislike
    }

    private List<Comment> comments;

    public NewsAdapter(Context context, int resource, List<News> news, String organizationId, List<String> likes, List<String> dislikes, List<Comment> comments) {
        super(context, resource, news);
        this.organizationId = organizationId;
        this.news = news;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.likes = likes;
        this.dislikes = dislikes;
        this.comments = comments;
        mAuth = FirebaseAuth.getInstance();
    }
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view=inflater.inflate(this.layout, parent, false);

        ImageView imgView = (ImageView) view.findViewById(R.id.image);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView textView = (TextView) view.findViewById(R.id.text);
        TextView dateView = (TextView) view.findViewById(R.id.date);
        TextView likesView = (TextView) view.findViewById(R.id.likesCount);
        TextView dislikesView = (TextView) view.findViewById(R.id.dislikesCount);
        ImageButton likesButton = (ImageButton) view.findViewById(R.id.likesButton);
        ImageButton dislikesButton = (ImageButton) view.findViewById(R.id.dislikesButton);
        ImageButton deleteButton=(ImageButton) view.findViewById(R.id.deleteButton);
        final News currentNews = news.get(position);
        Storage.downloadPicture(currentNews.getImgResource(), imgView);
        titleView.setText(currentNews.getTitle());
        dateView.setText(DataConverter.convert(currentNews.getDate()));
        textView.setText(currentNews.getText());
        likesView.setText(likes.get(position));
        dislikesView.setText(dislikes.get(position));

        final Comment comment = comments.get(position);

        if (comment == Comment.like) {
            likesButton.setImageResource(R.drawable.like_red);
        }
        else if (comment == Comment.dislike) {
            dislikesButton.setImageResource(R.drawable.dislike_red);
        }

        likesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (comment == Comment.like) {
                    DatabaseReference myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("likes").child(mAuth.getUid());
                    myRef3.removeValue();
                }
                else if (comment == Comment.dislike) {
                    NewsFragment.updateUIflag = false;
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("dislikes").child(mAuth.getUid());
                    myRef.removeValue();
                    DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("likes");
                    myRef2.child(mAuth.getUid()).child("info").setValue(0);
                }
                else {
                    DatabaseReference myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("likes");
                    myRef3.child(mAuth.getUid()).child("info").setValue(0);
                }
            }
        });

        dislikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (comment == Comment.like) {
                    NewsFragment.updateUIflag = false;
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("likes").child(mAuth.getUid());
                    myRef.removeValue();
                    DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("dislikes");
                    myRef2.child(mAuth.getUid()).child("info").setValue(0);
                }
                else if (comment == Comment.dislike) {
                    DatabaseReference myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("dislikes").child(mAuth.getUid());
                    myRef3.removeValue();
                }
                else {
                    DatabaseReference myRef3 = FirebaseDatabase.getInstance().getReference("organization")
                            .child(organizationId).child("news").child(Integer.toString(news.size() - 1 - position)).child("dislikes");
                    myRef3.child(mAuth.getUid()).child("info").setValue(0);
                }
            }
        });

        // не срабатывает с == при обновлении БД
        if (mAuth.getUid().contains(currentNews.getUID()))
        {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference("organization")
                           .child(organizationId).child("news")
                           .child(currentNews.getID()).removeValue();
                    Storage.deleteImg(currentNews.getImgResource());
                }
            });
        }
        else {
            deleteButton.setEnabled(false);
            deleteButton.setVisibility(View.INVISIBLE);
        }
        return view;
    }
}