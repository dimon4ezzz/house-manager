package com.dvor.my.mydvor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.dvor.my.mydvor.news.NewsFragment
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File


object Storage {

    fun downloadPicture(url: String, imgView: ImageView) {

        try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child(url)
            val localFile = File.createTempFile("image", "jpg")
            imageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                imgView.setImageBitmap(bitmap)
            }.addOnFailureListener {
                imgView.layoutParams.height = 0
                imgView.requestLayout()
            }
        } catch (ex: Exception) {
            Log.d("state", ex.message)
        }

    }

    fun uploadPicture(img: Bitmap, Url: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imagesRef = storageRef.child(Url)
        try {
            MainActivity.imgPref = img
            val baos = ByteArrayOutputStream()
            img.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = imagesRef.putBytes(data)

            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener { }
        } catch (ex: Exception) {
            Log.d("state", ex.message)
        }

    }

    fun deleteImg(imgUrl: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child(imgUrl)
        storageReference.delete().addOnSuccessListener {
            // File deleted successfully
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
        }
    }
}
