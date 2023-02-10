package com.example.notesapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notesapp.adapter.Adapter
import com.example.notesapp.database.DatabaseNote
import com.example.notesapp.entities.Note
import kotlinx.android.synthetic.main.fragment_home_page.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.io.*
import java.util.*
import kotlin.streams.toList
import kotlinx.serialization.decodeFromString as myJsonDecode

/**
 * A simple [Fragment] subclass.
 * Use the [HomePage.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomePage : Base() {
    // TODO: Rename and change types of parameters
    var arrNotes = ArrayList<Note>()
    var notesAdapter: Adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment HomePage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            HomePage().apply {
                arguments = Bundle().apply {

                }
            }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.setHasFixedSize(true)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter("action_del")
        )
        recycler_view.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        launch {
            context?.let {
                val dbNotes = DatabaseNote.getDatabase(it).noteDao().getAllNotes()
                var fileNotes: MutableList<Note?> = mutableListOf()

                val file = File(it.filesDir, ApplicationState.internalFile)
                fileNotes = Deserialize(file, it, fileNotes)
                if (ApplicationState.mode) {
                    tv1.text = "Notes (sql saving mode)"
                } else {
                    tv1.text = "Notes (file saving mode)"
                }
                //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val notes = dbNotes + fileNotes
                notes.sortedByDescending { note: Note? -> note?.dateTime }
                notesAdapter.setData(notes)
                recycler_view.adapter = notesAdapter
                arrNotes = notes as ArrayList<Note>
            }
        }

        imgChoose.setOnClickListener {
            showPopupMenu()
        }
        notesAdapter.setOnClickListener(onClicked)
        fabBtnCreateNote.setOnClickListener {
            replaceFragment(CreateNote.newInstance(), istransition = true)
        }


        search_view.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val tempArr = ArrayList<Note>()
                for (arr in arrNotes) {
                    if (arr.title!!.lowercase(Locale.getDefault())
                            .contains(newText.toString()) || arr.noteText!!.lowercase(Locale.getDefault())
                            .contains(newText.toString())
                    ) {
                        tempArr.add(arr)
                    }
                }
                notesAdapter.setData(tempArr)
                notesAdapter.notifyDataSetChanged()
                return true
            }
        })
    }

    fun Deserialize(
        file: File,
        it: Context,
        fileNotes: MutableList<Note?>
    ): MutableList<Note?> {
        var fileNotes1 = fileNotes
        if (file.exists()) {
            val fileInputStream: FileInputStream =
                it.openFileInput(ApplicationState.internalFile)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val lines = bufferedReader.lines().toList()

            fileNotes1 = lines.map { line -> Json.myJsonDecode(line) as Note }.toMutableList()
        }
        return fileNotes1
    }

    @SuppressLint("SetTextI18n")
    private fun showPopupMenu() {
        val popupMenu = PopupMenu(requireContext(), imgChoose)
        popupMenu.inflate(R.menu.menu_main)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.sql -> {
                    Toast.makeText(requireContext(), "sql", Toast.LENGTH_SHORT).show()
                    ApplicationState.mode = true
                    tv1.text = "Notes (sql saving mode)"
                }
                R.id.internal -> {
                    Toast.makeText(requireContext(), "internal", Toast.LENGTH_SHORT).show()
                    ApplicationState.mode = false
                    tv1.text = "Notes (file saving mode)"
                }
            }
            Log.d("NotesApp", "Mode switched to ${ApplicationState.mode}")
            true
        }
    }

    private val onClicked = object : Adapter.OnItemClickListener {
        override fun onClicked(noteId: Long, sql_based: Boolean) {

            val fragment: Fragment
            val bundle = Bundle()
            bundle.putLong("noteId", noteId)
            bundle.putBoolean("sql_based", sql_based)
            fragment = CreateNote.newInstance()
            fragment.arguments = bundle

            replaceFragment(fragment, false)
        }
    }


    fun deleteNote(intent: Intent) {
        val noteId: Long = intent.getLongExtra("noteId", -1L)
        val sqlBased: Boolean = intent.getBooleanExtra("sql_based", true)
        launch(Dispatchers.IO) {
            context?.let {
                if (sqlBased) {
                    DatabaseNote.getDatabase(it).noteDao().deleteSpecificNote(noteId)
                    Log.d("NotesApp", "deleted from database $noteId")
                } else {
                    var fileNotes: MutableList<Note?> = mutableListOf()
                    val file = File(it.filesDir, ApplicationState.internalFile)
                    fileNotes = Deserialize(file, it, fileNotes)
                    fileNotes.removeIf { note -> note?.id == noteId }

                    val fileOutputStream: FileOutputStream = requireContext().openFileOutput(
                        ApplicationState.internalFile,
                        Context.MODE_PRIVATE
                    )
                    for (note in fileNotes) {
                        CreateNote.newInstance().SerializeAndWrite(note!!, fileOutputStream)
                    }
                    Log.d("NotesApp", "deleted from file $noteId")
                }
                withContext(Dispatchers.Main) {
                    val target = if (sqlBased) "database" else "file"
                    Toast.makeText(context, "Deleted from $target", Toast.LENGTH_SHORT).show()
                    intent.putExtra("inProgress", false)
                }
            }
        }
    }

    fun replaceFragment(fragment: Fragment, istransition: Boolean) {
        val fragmentTransition = requireActivity().supportFragmentManager.beginTransaction()

        if (istransition) {
            fragmentTransition.setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
        }
        fragmentTransition.add(R.id.frame_layout, fragment)
            .addToBackStack(fragment.javaClass.simpleName).commit()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val action = intent!!.getStringExtra("action")
            when (action!!) {
                "DeleteNote" -> {
                    if (!intent.getBooleanExtra("inProgress", false)) {
                        intent.putExtra("inProgress", true)
                        deleteNote(intent)
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}
