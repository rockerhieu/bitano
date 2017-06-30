package io.github.rockerhieu.duet

import android.app.Activity
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class MainActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val notesRef = database.getReference("notes")
    val noteLabel by bind<EditText>(R.id.noteLabel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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