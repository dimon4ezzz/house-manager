package com.dvor.my.mydvor.news

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.dvor.my.mydvor.MainActivity
import com.dvor.my.mydvor.MyEvent
import com.dvor.my.mydvor.MyEventListener
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.Storage
import com.dvor.my.mydvor.Type
import com.dvor.my.mydvor.data.News
import com.dvor.my.mydvor.data.NewsBD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.LinkedList

class NewsFragment : Fragment() {

    private var mAuth: FirebaseAuth? = null
    internal var userStreetId: String = ""
    internal var organizationId: String = ""
    internal var userBuildingId: String = ""
    internal var userName: String = ""
    internal var myRef2: DatabaseReference? = null
    internal var myRef3: DatabaseReference? = null
    internal var newsSnapshot: DataSnapshot? = null
    internal var listenerBuilding: ValueEventListener? = null
    internal var listenerNews: ValueEventListener? = null
    private var externalView: View? = null
    private val news = ArrayList<News>()
    private val likes = ArrayList<String>()
    private val dislikes = ArrayList<String>()
    private val comments = ArrayList<NewsAdapter.Comment>()
    private var imgID = "newsImages/no"
    internal lateinit var newsList: ListView
    internal var itemListener: AdapterView.OnItemClickListener? = null
    internal lateinit var context: Context
    private var postText: TextView? = null
    internal var imageUri: Uri? = null
    internal var lastID = 0
    internal var index: Int = 0
    internal var top: Int = 0

    fun addEventListener(eventListener: MyEventListener) {
        eventListeners!!.add(eventListener)
    }

    fun notifyEventListeners(event: MyEvent) {
        for (eventListener in eventListeners!!) {
            eventListener.processEvent(event)
        }
    }

    //сохранение текста неотправленного сообщения
    //костыльно но работает
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("postText", postText!!.text.toString())
        outState.putString("postImg", imgID)
        MainActivity.savedPost = postText!!.text.toString()
        MainActivity.postImg = imgID
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        updateUIflag = true

        if (eventListeners == null) {
            eventListeners = LinkedList()
        } else {
            eventListeners!!.clear()
        }

        externalView = inflater.inflate(R.layout.fragment_news, container, false)
        // начальная инициализация списка
        // получаем элемент ListView
        newsList = externalView!!.findViewById(R.id.newsList)

        // создаем адаптер
        context = externalView!!.context

        val button = externalView!!.findViewById<Button>(R.id.send_post)
        button.setOnClickListener { v ->
            if (v.id == R.id.send_post) {
                sendPost()
            }
        }

        val imgbutton = externalView!!.findViewById<ImageButton>(R.id.add_img)
        imgbutton.setOnClickListener { v ->
            when (v.id) {
                R.id.add_img -> addImage()
                R.id.imgPref -> {
                    deletePrefImg()
                }
                else -> {
                }
            }
        }

        val imgPref = externalView!!.findViewById<ImageButton>(R.id.imgPref)
        val preImg = BitmapDrawable(context.resources, MainActivity.imgPref)
        imgPref.background = preImg
        imgPref.setOnClickListener { v ->
            if (v.id == R.id.imgPref) {
                deletePrefImg()
            }
        }
        if (MainActivity.imgPref != null)
            imgPref.setImageResource(R.drawable.delete)


        postText = externalView!!.findViewById(R.id.post_text)
        mAuth = FirebaseAuth.getInstance()

        postText!!.text = MainActivity.savedPost
        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth!!.uid!!)
        imgID = MainActivity.postImg

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null) {
                    userName = (dataSnapshot.child("name").value!!.toString()
                            + " " + dataSnapshot.child("surname").value!!.toString())
                    userStreetId = dataSnapshot.child("street_id").value!!.toString()
                    userBuildingId = dataSnapshot.child("building_id").value!!.toString()

                    notifyEventListeners(MyEvent(this, Type.UpdateAddressID))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        this.addEventListener(object : MyEventListener {
            override fun processEvent(event: MyEvent) {
                if (event.source == null) {
                    return
                }

                when (event.type) {
                    Type.UpdateAddressID -> {

                        if (myRef2 != null) {
                            myRef2!!.removeEventListener(listenerBuilding!!)
                        }

                        myRef2 = FirebaseDatabase.getInstance().getReference("streets").child(userStreetId).child("buildings").child(userBuildingId).child("organization_id")

                        listenerBuilding = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                organizationId = dataSnapshot.value!!.toString()

                                notifyEventListeners(MyEvent(this, Type.UpdateOrganizationId))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef2!!.addValueEventListener(listenerBuilding!!)
                    }

                    Type.UpdateOrganizationId -> {

                        if (myRef3 != null) {
                            myRef3!!.removeEventListener(listenerNews!!)
                        }

                        myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news")

                        listenerNews = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                newsSnapshot = dataSnapshot

                                notifyEventListeners(MyEvent(this, Type.UpdateNews))
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        }

                        myRef3!!.addValueEventListener(listenerNews!!)
                    }

                    Type.UpdateNews -> {
                        if (updateUIflag) {
                            updateUI()
                            System.gc()
                        } else {
                            updateUIflag = true
                        }
                    }
                }
            }
        })

        return externalView
    }

    private fun deletePrefImg() {
        MainActivity.imgPref = null
        imgID = "newsImages/no"
        val imgPref = externalView!!.findViewById<ImageButton>(R.id.imgPref)
        imgPref.background = null
        MainActivity.postImg = "newsImages/no"

        imgPref.setImageResource(R.drawable.transparent)
    }

    private fun addImage() {
        imgID = "newsImages/" + (lastID + 1) + ".jpg"
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, RESULT_GALLERY)
    }

    private fun sendPost() {
        val newsId = lastID + 1

        if (postText!!.text.toString().trim { it <= ' ' }.isEmpty())
            return

        val mes = NewsBD(userName, postText!!.text.toString(),
                Date().toString(), imgID, mAuth!!.uid.toString())

        postText!!.text = ""
        MainActivity.savedPost = ""
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        FirebaseDatabase.getInstance().getReference("organization")
                .child(organizationId).child("news")
                .child(newsId.toString()).setValue(mes)
        Toast.makeText(activity!!.applicationContext, "Ваша новость опубликована",
                Toast.LENGTH_SHORT).show()
        deletePrefImg()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_GALLERY) {
            if (null != data) {

                imageUri = data.data
                try {
                    val imageStream = activity!!.contentResolver.openInputStream(imageUri!!)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    Storage.uploadPicture(selectedImage, imgID)
                    val imgPref = externalView!!.findViewById<ImageButton>(R.id.imgPref)
                    imgPref.setImageResource(R.drawable.delete)
                } catch (ex: Exception) {
                    Log.d("state", ex.message)
                }

            }
        }
    }

    private fun updateUI() {
        // сохраняем видимую позицию listView
        index = newsList.firstVisiblePosition
        val v = newsList.getChildAt(0)
        top = if (v == null) 0 else v.top - newsList.paddingTop

        news.clear()
        likes.clear()
        dislikes.clear()
        comments.clear()
        if (newsSnapshot != null) {
            for (n in newsSnapshot!!.children) {

                news.add(News(n.key.toString(),
                        n.child("header").value!!.toString(),
                        n.child("text").value!!.toString(), n.child("date").value!!.toString(),
                        n.child("img").value!!.toString(), n.child("uID").value.toString()))

                lastID = Integer.parseInt(n.key.toString())
                var countLikes: Long = 0
                var countDislikes: Long = 0
                var comment: NewsAdapter.Comment = NewsAdapter.Comment.absent

                val likesSnapshot = n.child("likes")
                val dislikesSnapshot = n.child("dislikes")

                if (likesSnapshot.value != null) {
                    countLikes = likesSnapshot.childrenCount
                }

                if (dislikesSnapshot.value != null) {
                    countDislikes = dislikesSnapshot.childrenCount
                }

                if (likesSnapshot.child(mAuth!!.uid!!).value != null) {
                    comment = NewsAdapter.Comment.like
                } else if (dislikesSnapshot.child(mAuth!!.uid!!).value != null) {
                    comment = NewsAdapter.Comment.dislike
                }

                likes.add(countLikes.toString())
                dislikes.add(countDislikes.toString())
                comments.add(comment)
            }

        }

        // отображаем сначала новые
        news.reverse()
        likes.reverse()
        dislikes.reverse()
        comments.reverse()
        val newsAdapter = NewsAdapter(activity, R.layout.list_news, news, organizationId, likes, dislikes, comments)
        // устанавливаем адаптер
        newsList.adapter = newsAdapter

        if (itemListener == null) {
            // слушатель выбора в списке
            itemListener = AdapterView.OnItemClickListener { parent, v, position, id ->
                // получаем выбранный пункт
                val selectedNews = parent.getItemAtPosition(position) as News
                Toast.makeText(activity!!.applicationContext, "Был выбран пункт " + selectedNews.title!!,
                        Toast.LENGTH_SHORT).show()
            }
            newsList.onItemClickListener = itemListener
        }
        //перематывает listView на ранее открытую позицию
        newsList.setSelectionFromTop(index, top)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventListeners!!.clear()
        if (myRef2 != null) {
            myRef2!!.removeEventListener(listenerBuilding!!)
        }
        if (myRef3 != null) {
            myRef3!!.removeEventListener(listenerNews!!)
        }
        listenerBuilding = null
        listenerNews = null
    }

    companion object {

        private var eventListeners: MutableList<MyEventListener>? = null
        var updateUIflag: Boolean = false
        val RESULT_GALLERY = 0
    }
}