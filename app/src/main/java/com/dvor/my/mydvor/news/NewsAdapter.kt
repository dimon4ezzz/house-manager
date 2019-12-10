package com.dvor.my.mydvor.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.Storage
import com.dvor.my.mydvor.data.News
import com.dvor.my.mydvor.utils.DateConverter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NewsAdapter(context: FragmentActivity?, private val layout: Int, private val news: List<News>, private var organizationId: String, private val likes: List<String>, private val dislikes: List<String>, private val comments: List<Comment>) : ArrayAdapter<News>(context!!, layout, news) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()


    enum class Comment {
        Absent, Like, Dislike
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = inflater.inflate(this.layout, parent, false)

        val imgView = view.findViewById<ImageView>(R.id.image)
        val titleView = view.findViewById<TextView>(R.id.title)
        val textView = view.findViewById<TextView>(R.id.text)
        val dateView = view.findViewById<TextView>(R.id.date)
        val likesView = view.findViewById<TextView>(R.id.likesCount)
        val dislikesView = view.findViewById<TextView>(R.id.dislikesCount)
        val likesButton = view.findViewById<ImageButton>(R.id.likesButton)
        val dislikesButton = view.findViewById<ImageButton>(R.id.dislikesButton)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)
        val currentNews = news[position]

        if (currentNews.imgResource != "newsImages/no" && currentNews.imgResource != "null") {
            imgView.visibility = View.VISIBLE
            Storage.downloadPicture(currentNews.imgResource, imgView)
        } else {
            imgView.visibility = View.GONE
        }

        titleView.text = currentNews.title
        dateView.text = DateConverter.convert(currentNews.date.toString())
        dateView.setOnClickListener {
            Toast.makeText(context, currentNews.date.toString(), Toast.LENGTH_SHORT).show()
        }
        textView.text = currentNews.text
        likesView.text = likes[position]
        dislikesView.text = dislikes[position]

        val comment = comments[position]

        if (comment == Comment.Like) {
            likesButton.setImageResource(R.drawable.like_red)
        } else if (comment == Comment.Dislike) {
            dislikesButton.setImageResource(R.drawable.dislike_red)
        }

        likesButton.setOnClickListener {
            when (comment) {
                Comment.Like -> {
                    val myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("likes").child(mAuth.uid!!)
                    myRef3.removeValue()
                }
                Comment.Dislike -> {
                    NewsFragment.updateUIflag = false
                    val myRef = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("dislikes").child(mAuth.uid!!)
                    myRef.removeValue()
                    val myRef2 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("likes")
                    myRef2.child(mAuth.uid!!).child("info").setValue(0)
                }
                else -> {
                    val myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("likes")
                    myRef3.child(mAuth.uid!!).child("info").setValue(0)
                }
            }
        }

        dislikesButton.setOnClickListener {
            when (comment) {
                Comment.Like -> {
                    NewsFragment.updateUIflag = false
                    val myRef = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("likes").child(mAuth.uid!!)
                    myRef.removeValue()
                    val myRef2 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("dislikes")
                    myRef2.child(mAuth.uid!!).child("info").setValue(0)
                }
                Comment.Dislike -> {
                    val myRef3 = FirebaseDatabase.getInstance().getReference("organization").child(organizationId).child("news").child((news.size - 1 - position).toString()).child("dislikes").child(mAuth.uid!!)
                    myRef3.removeValue()
                }
                else -> {
                    val myRef3 = FirebaseDatabase.getInstance().getReference("organization")
                            .child(organizationId).child("news").child((news.size - 1 - position).toString()).child("dislikes")
                    myRef3.child(mAuth.uid!!).child("info").setValue(0)
                }
            }
        }

        // не срабатывает с == при обновлении БД
        if (mAuth.uid!!.contains(currentNews.uid.toString())) {
            deleteButton.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("organization")
                        .child(organizationId).child("news")
                        .child(currentNews.id).removeValue()
                Storage.deleteImg(currentNews.imgResource)
            }
        } else {
            deleteButton.isEnabled = false
            deleteButton.visibility = View.INVISIBLE
        }
        return view
    }
}