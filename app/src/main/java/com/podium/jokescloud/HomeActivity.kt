package com.podium.jokescloud

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.podium.jokescloud.model.Joke

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fab: FloatingActionButton
    private lateinit var dataRecycler: RecyclerView
    private lateinit var textEmail: TextView
    private lateinit var jokeList: ArrayList<Joke>

    companion object {

        private const val COLLECTION_JOKES = "jokes"
        private const val TAG = "HomeActivity"
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        auth = Firebase.auth
        db = Firebase.firestore
        fab = findViewById(R.id.fab)
        jokeList = arrayListOf()  //blank
        dataRecycler = findViewById(R.id.datalist)
        dataRecycler.layoutManager = LinearLayoutManager(this)
        val jokeAdapter = JokeAdapter(jokeList)
        dataRecycler.adapter = jokeAdapter
        textEmail = findViewById(R.id.textEmail)
        textEmail.text = auth.currentUser?.email ?: "no email"
        fab.setOnClickListener {
            showPopupDialog()

        }

        val docRef = db.collection(COLLECTION_JOKES)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Snackbar.make(fab, e.message!!, Snackbar.LENGTH_SHORT).show()
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                jokeList.clear()
                for (doc in snapshot){
                    jokeList.add(doc.toObject<Joke>())
                }
                jokeAdapter.notifyDataSetChanged()
            } else {
                Snackbar.make(fab, "data is null", Snackbar.LENGTH_SHORT).show()
                Log.d(TAG, "Current data: null")
            }
        }

    }

    private fun showPopupDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tell us Joke")
        val input = EditText(this)
        input.hint = "something funny!"
        input.inputType = InputType.TYPE_CLASS_TEXT

        builder.setView(input)
        builder.setPositiveButton("OK") { d, i ->
            val jokeText = input.text.toString()
            db.collection(COLLECTION_JOKES).add(
                Joke(
                    jokeText, auth.currentUser!!.uid, auth.currentUser!!.email!!
                )
            )
                .addOnSuccessListener {
                    Snackbar.make(fab, "joke added successfully", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Snackbar.make(fab, it.message!!, Snackbar.LENGTH_SHORT).show()
                }
        }
        builder.setNegativeButton("Cancel") { d, i ->
            d.cancel() // close the dialog
        }
        builder.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_signout) {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
        }
        return super.onOptionsItemSelected(item)
    }
}