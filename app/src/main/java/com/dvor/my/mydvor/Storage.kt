package com.dvor.my.mydvor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File


object Storage {

    fun downloadPicture(context: Context, url: String, imgView: ImageView) {
        val buf = url.split('/')
        val filename = buf.last()
        val path = url.dropLast(filename.length)

        var bufPath = context.getCacheDir().toString()

        for(el in buf.dropLast(1)) {
            bufPath += "/" + el

            val f = File(bufPath)

            if(!f.exists()) {
                f.mkdir()
            }
        }

        try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child(url)
            val file = File(context.getCacheDir().toString() + "/" + path, filename);
            if(!file.exists()) {
                file.createNewFile()
                imageRef.getFile(file).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imgView.setImageBitmap(bitmap)
                }.addOnFailureListener {
                    imgView.layoutParams.height = 0
                    imgView.requestLayout()
                }
            }
            else {
                imgView.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            }
        } catch (ex: Exception) {
            Log.d("state", ex.message.toString())
        }
    }

    fun uploadPicture(img: Bitmap, Url: String, f: () -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imagesRef = storageRef.child(Url)
        try {
            MainActivity.imgPref = img
            val baos = ByteArrayOutputStream()
            img.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask = imagesRef.putBytes(data)

            uploadTask.addOnSuccessListener {
                f()
            }.addOnFailureListener {
                Log.d("state", "failure upload")
            }
        } catch (ex: Exception) {
            Log.d("state", ex.message.toString())
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
