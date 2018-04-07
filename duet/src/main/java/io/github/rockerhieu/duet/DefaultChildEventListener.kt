package io.github.rockerhieu.duet

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

/**
 * Created by rockerhieu on 6/30/17.
 */
open class DefaultChildEventListener : ChildEventListener {
    override fun onCancelled(e: DatabaseError?) {
    }

    override fun onChildMoved(data: DataSnapshot, previousChildName: String?) {
    }

    override fun onChildChanged(data: DataSnapshot, previousChildName: String?) {
    }

    override fun onChildAdded(data: DataSnapshot, previousChildName: String?) {
    }

    override fun onChildRemoved(data: DataSnapshot) {
    }
}