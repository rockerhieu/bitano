package io.github.rockerhieu.duet

import android.app.Activity
import android.app.AlarmManager
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val notesRef = database.getReference("notes")
    val logsRef = database.getReference("logs")
    val noteLabel by bind<EditText>(R.id.noteLabel)
    val labels by bind<TextView>(R.id.label)
    val image by bind<ImageView>(R.id.image)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        watchForLogs()
    }

    private fun watchForLogs() {
        logsRef.limitToLast(1).addChildEventListener(object :
                DefaultChildEventListener() {
            override fun onChildAdded(data: DataSnapshot, previousChildName: String?) {
                val imageUrl = data.child("image").getValue(String::class.java)
                val imageLabels = data.child("labels").getValue(String::class.java)

                Picasso.get().load(imageUrl).into(image)
                labels.text = imageLabels
            }
        })
    }

    fun onPlayClick(v: View) {
        notesRef.push()
                .setValue(noteLabel.text.toString(), ServerValue.TIMESTAMP)
    }
}

fun <T : View> Activity.bind(@IdRes res: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy { findViewById(res) as T }
}