package com.dvor.my.mydvor.news

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.*
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.News
import com.dvor.my.mydvor.data.NewsBD
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class NewsFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    internal var userStreetId: String = ""
    internal var organizationId: String = ""
    internal var userBuildingId: String = ""
    internal var userName: String = ""
    internal var myRef2: DatabaseReference? = null
    internal var myRef3: DatabaseReference? = null
    internal var newsSnapshot: DataSnapshot? = null
    internal var listenerBuilding: ValueEventListener? = null
    internal var listenerNews: ValueEventListener? = null
    private val news = ArrayList<News>()
    private val likes = ArrayList<String>()
    private val dislikes = ArrayList<String>()
    private val comments = ArrayList<NewsAdapter.Comment>()
    private var imgID = "newsImages/no"
    private lateinit var newsList: ListView
    internal lateinit var context: Context
    private var postText: TextView? = null
    private var imageUri: Uri? = null
    private var lastID = 0
    private var index: Int = 0
    private var top: Int = 0

    private fun addEventListener(eventListener: MyEventListener) {
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
        if (postText == null) {
            outState.putString("postText", MainActivity.savedPost)
        } else {
            outState.putString("postText", postText!!.text.toString())
            MainActivity.savedPost = postText!!.text.toString()
        }

        outState.putString("postImg", imgID)
        MainActivity.postImg = imgID
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        updateUIflag = true

        if (eventListeners == null) {
            eventListeners = LinkedList()
        } else {
            eventListeners!!.clear()
        }

        val view = inflater.inflate(R.layout.fragment_news, container, false)
        // начальная инициализация списка
        // получаем элемент ListView
        newsList = view!!.findViewById(R.id.lv_news)

        // создаем адаптер
        context = view.context

        view.findViewById<Button>(R.id.b_send).setOnClickListener { v ->
            if (v.id == R.id.b_send) {
                sendPost()
            }
        }

        view.findViewById<ImageButton>(R.id.im_add_image).setOnClickListener { v ->
            when (v.id) {
                R.id.im_add_image -> addImage()
                R.id.ib_delete_image -> deletePrefImg()
            }
        }

        val deleteBtn = view.findViewById<ImageButton>(R.id.ib_delete_image)
        deleteBtn.setOnClickListener { v ->
            if (v.id == R.id.ib_delete_image) {
                deletePrefImg()
            }
        }
        deleteBtn.visibility = View.VISIBLE

        postText = view.findViewById(R.id.et_post)
        mAuth = FirebaseAuth.getInstance()

        postText!!.text = MainActivity.savedPost
        val myRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.uid!!)
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

        return view
    }

    private fun deletePrefImg() {
        MainActivity.imgPref = null
        imgID = "newsImages/no"
        view!!.findViewById<ImageButton>(R.id.ib_delete_image).visibility = View.GONE
        MainActivity.postImg = "newsImages/no"
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
                Date().toString(), imgID, mAuth.uid.toString())

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

        if (requestCode == RESULT_GALLERY && data != null) {
            // remember image data
            imageUri = data.data

            try {
                // open stream for image data
                val imageStream = activity!!.contentResolver.openInputStream(imageUri!!)
                // decode this as bitmap
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                // upload picture to firebase
                Storage.uploadPicture(selectedImage, imgID)
                // show `delete uploaded picture` button
                view!!.findViewById<ImageButton>(R.id.ib_delete_image).visibility = View.VISIBLE
            } catch (ex: Exception) {
                Log.d("state", ex.message.toString())
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
                        n.child("header").value.toString(),
                        n.child("text").value.toString(), n.child("date").value.toString(),
                        n.child("img").value.toString(), n.child("uid").value.toString()))

                var countLikes: Long = 0
                var countDislikes: Long = 0
                var comment: NewsAdapter.Comment = NewsAdapter.Comment.Absent

                val likesSnapshot = n.child("likes")
                val dislikesSnapshot = n.child("dislikes")

                if (likesSnapshot.value != null) {
                    countLikes = likesSnapshot.childrenCount
                }

                if (dislikesSnapshot.value != null) {
                    countDislikes = dislikesSnapshot.childrenCount
                }

                if (likesSnapshot.child(mAuth.uid!!).value != null) {
                    comment = NewsAdapter.Comment.Like
                } else if (dislikesSnapshot.child(mAuth.uid!!).value != null) {
                    comment = NewsAdapter.Comment.Dislike
                }

                likes.add(countLikes.toString())
                dislikes.add(countDislikes.toString())
                comments.add(comment)
            }
            lastID = Integer.parseInt(newsSnapshot!!.children.count().toString())
        }

        // отображаем сначала новые
        news.reverse()
        likes.reverse()
        dislikes.reverse()
        comments.reverse()
        val newsAdapter = NewsAdapter(activity, R.layout.list_news, news, organizationId, likes, dislikes, comments)
        // устанавливаем адаптер
        newsList.adapter = newsAdapter

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
        const val RESULT_GALLERY = 0
    }
}