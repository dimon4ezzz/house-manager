
package com.dvor.my.mydvor;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NewsFragment extends Fragment
 {

    private static List<MyEventListener> eventListeners;

    public void addEventListener(MyEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void notifyEventListeners(MyEvent event) {
        for (MyEventListener eventListener: eventListeners) {
            eventListener.processEvent(event);
        }
    }

    private FirebaseAuth mAuth;
    String userStreetId;
    String organizationId;
    String userBuildingId;
    String userName;
    DatabaseReference myRef2;
    DatabaseReference myRef3;
    DataSnapshot newsSnapshot;
    ValueEventListener listenerBuilding;
    ValueEventListener listenerNews;
    public static boolean updateUIflag;
    private View view;
    private List<News> news = new ArrayList<News>();
    private List<String> likes = new ArrayList<String>();
    private List<String> dislikes = new ArrayList<String>();
    private List<NewsAdapter.Comment> comments = new ArrayList<NewsAdapter.Comment>();
    private String imgID="newsImages/no";
    ListView newsList;
    AdapterView.OnItemClickListener itemListener;
    Context context;
    private TextView postText;
    Uri imageUri;
    public  static final int RESULT_GALLERY = 0;
    int lastID=0;
    int index;
    int top;

     //сохранение текста неотправленного сообщения
     //костыльно но работает
     public void onSaveInstanceState(Bundle outState) {
         outState.putString("postText", (postText.getText().toString()));
         outState.putString("postImg", imgID);
         MainActivity.savedPost=postText.getText().toString();
         MainActivity.postImg=imgID;
     }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        updateUIflag = true;

        if(eventListeners == null) {
            eventListeners = new LinkedList<>();
        }
        else {
            eventListeners.clear();
        }

        view = inflater.inflate(R.layout.fragment_news, container, false);
        // начальная инициализация списка
        // получаем элемент ListView
        newsList = (ListView) view.findViewById(R.id.newsList);

        // создаем адаптер
        context = view.getContext();

        Button button = (Button) view.findViewById(R.id.send_post);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.send_post:
                        sendPost();
                        break;
                    default:
                        break;
                }
            }
        });

        ImageButton imgbutton = (ImageButton) view.findViewById(R.id.add_img);
        imgbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.add_img:
                        addImage();
                        break;
                    case R.id.imgPref: {
                        deletePrefImg();
                        break;
                    }
                    default:
                        break;
                }
            }
        });

        ImageButton imgPref = (ImageButton) view.findViewById(R.id.imgPref);
        BitmapDrawable preImg = new BitmapDrawable(context.getResources(),MainActivity.imgPref);
        imgPref.setBackground(preImg);
        imgPref.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View v) {
              switch (v.getId()) {
                  case R.id.imgPref: {
                       deletePrefImg();
                    }
                       break;
                    default:
                        break;
               }
            }
        });
        if (MainActivity.imgPref!=null)
            imgPref.setImageResource(R.drawable.delete);


        postText = (TextView)view.findViewById(R.id.post_text) ;
        mAuth = FirebaseAuth.getInstance();

        postText.setText(MainActivity.savedPost);
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getUid());
        imgID=MainActivity.postImg;

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    userName=dataSnapshot.child("name").getValue().toString()
                    +" "+dataSnapshot.child("surname").getValue().toString();
                    userStreetId = dataSnapshot.child("street_id").getValue().toString();
                    userBuildingId = dataSnapshot.child("building_id").getValue().toString();

                    notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateAddressID));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        this.addEventListener(new MyEventListener() {
            @Override
            public void processEvent(MyEvent event) {
                if(event.getSource() == null || event.getType() == null) {
                    return;
                }

                switch (event.getType()) {
                    case UpdateAddressID: {

                        if(myRef2 != null) {
                            myRef2.removeEventListener(listenerBuilding);
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId).child("buildings").child(userBuildingId).child("organization_id");

                        listenerBuilding = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                organizationId = dataSnapshot.getValue().toString();

                                notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateOrganizationId));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef2.addValueEventListener(listenerBuilding);
                    } break;

                    case UpdateOrganizationId: {

                        if(myRef3 != null) {
                            myRef3.removeEventListener(listenerNews);
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news");

                        listenerNews = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                newsSnapshot = dataSnapshot;

                                notifyEventListeners(new MyEvent(this, MyEvent.Type.UpdateNews));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        myRef3.addValueEventListener(listenerNews);
                    } break;

                    case UpdateNews: {
                        if(updateUIflag) {
                            updateUI();
                            System.gc();
                        } else {
                            updateUIflag = true;
                        }
                    } break;
                }

            }
        });

        return view;
    }

    private void deletePrefImg()
    {
        MainActivity.imgPref = null;
        imgID="newsImages/no";
        ImageButton imgPref = (ImageButton) view.findViewById(R.id.imgPref);
        imgPref.setBackground(null);
        MainActivity.postImg="newsImages/no";

        imgPref.setImageResource(R.drawable.transparent);
    }
    private void addImage(){
        imgID ="newsImages/"+Integer.toString(lastID+1)+".jpg";
         final int RESULT_LOAD_IMAGE = 1;
         Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent , RESULT_GALLERY );
    }

    private void sendPost()
    {
        int newsId=lastID+1;

        if (postText.getText().toString()==null)
           return;
        if (postText.getText().toString().trim().length() == 0)
           return;

       NewsBD mes=new NewsBD(userName, postText.getText().toString(),
               (new Date()).toString(), imgID, mAuth.getUid());

       postText.setText("");
       MainActivity.savedPost="";
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       FirebaseDatabase.getInstance().getReference("organization")
              .child(organizationId).child("news")
               .child(Integer.toString(newsId)).setValue(mes);
       Toast.makeText(getActivity().getApplicationContext(), "Ваша новость опубликована",
               Toast.LENGTH_SHORT).show();
       deletePrefImg();
    }

     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         switch (requestCode) {
             case RESULT_GALLERY :
                 if (null != data) {

                     imageUri = data.getData();
                     try{
                     final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                     final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                     Storage.uploadPicture(selectedImage, imgID);
                     ImageButton imgPref = (ImageButton) view.findViewById(R.id.imgPref);
                     imgPref.setImageResource(R.drawable.delete);}
                     catch (Exception ex)
                     {
                         Log.d("state", ex.getMessage());
                     }
                 }
                 break;
             default:
                 break;
         }
     }

    private void updateUI(){
        // сохраняем видимую позицию listView
        index = newsList.getFirstVisiblePosition();
        View v = newsList.getChildAt(0);
        top = (v == null) ? 0 : (v.getTop() - newsList.getPaddingTop());

        news.clear();
        likes.clear();
        dislikes.clear();
        comments.clear();
        if(newsSnapshot != null) {
            for (DataSnapshot n : newsSnapshot.getChildren()) {

                news.add(new News(n.getKey(),
                        n.child("header").getValue().toString(),
                        n.child("text").getValue().toString(), n.child("date").getValue().toString(),
                        n.child("img").getValue().toString(), n.child("uID").getValue().toString()));

                lastID=Integer.parseInt(n.getKey());
                long countLikes = 0;
                long countDislikes = 0;
                NewsAdapter.Comment comment = NewsAdapter.Comment.absent;

                DataSnapshot likesSnapshot = n .child("likes");
                DataSnapshot dislikesSnapshot = n .child("dislikes");

                if(likesSnapshot.getValue() != null){
                    countLikes = likesSnapshot.getChildrenCount();
                }

                if(dislikesSnapshot.getValue() != null){
                    countDislikes = dislikesSnapshot.getChildrenCount();
                }

                if(likesSnapshot.child(mAuth.getUid()).getValue() != null){
                    comment = NewsAdapter.Comment.like;
                }
                else if(dislikesSnapshot.child(mAuth.getUid()).getValue() != null){
                    comment = NewsAdapter.Comment.dislike;
                }

                likes.add(Long.toString(countLikes));
                dislikes.add(Long.toString(countDislikes));
                comments.add(comment);
            }

        }

        // отображаем сначала новые
        Collections.reverse(news);
        Collections.reverse(likes);
        Collections.reverse(dislikes);
        Collections.reverse(comments);
        NewsAdapter newsAdapter = new NewsAdapter(getActivity(), R.layout.list_news, news, organizationId, likes, dislikes, comments);
        // устанавливаем адаптер
        newsList.setAdapter(newsAdapter);

        if(itemListener == null) {
            // слушатель выбора в списке
            itemListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    // получаем выбранный пункт
                    News selectedNews = (News) parent.getItemAtPosition(position);
                    Toast.makeText(getActivity().getApplicationContext(), "Был выбран пункт " + selectedNews.getTitle(),
                            Toast.LENGTH_SHORT).show();
                }
            };
            newsList.setOnItemClickListener(itemListener);
        }
        //перематывает listView на ранее открытую позицию
        newsList.setSelectionFromTop(index, top);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        eventListeners.clear();
        if(myRef2 != null) {
            myRef2.removeEventListener(listenerBuilding);
        }
        if(myRef3 != null) {
            myRef3.removeEventListener(listenerNews);
        }
        listenerBuilding=null;
        listenerNews=null;
    }
}