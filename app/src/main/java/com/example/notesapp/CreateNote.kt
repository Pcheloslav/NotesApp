package com.example.notesapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.notesapp.database.DatabaseNote
import com.example.notesapp.entities.Note
import com.example.notesapp.util.BottomSheet
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.lang.System.currentTimeMillis
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.encodeToString as myJsonEncode


class CreateNote : Base() {
    var selectedColor = "#202020"
    var currentDate: String? = null
    var istitle: Boolean = true
    var isnote: Boolean = true
    private var noteId: Long = -1
    private var sql_based: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        noteId = requireArguments().getLong("noteId", -1)
        sql_based = requireArguments().getBoolean("sql_based", true)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_note, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            CreateNote().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (noteId != -1L) {
            launch {
                context?.let {
                    if (sql_based) {
                        val notes = DatabaseNote.getDatabase(it).noteDao().getSpecificNote(noteId)
                        NoteTitle.setText(notes.title)
                        Notetext.setText(notes.noteText)
                    } else {
                        var fileNotes: MutableList<Note?> = mutableListOf()

                        val file = File(it.filesDir, ApplicationState.internalFile)
                        fileNotes = HomePage.newInstance().Deserialize(file, it, fileNotes)
                        val note = fileNotes.find { note: Note? -> note?.id == noteId }
                        NoteTitle.setText(note?.title)
                        Notetext.setText(note?.noteText)
                    }

                }
            }
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver, IntentFilter("bottom_sheet_action")
        )
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        currentDate = LocalDateTime.now().format(formatter)
        DateTime.text = currentDate

        icDone.setOnClickListener {
            if (noteId != -1L) {
                updateNote()
            } else {
                saveNote()
            }

        }


        icBack.setOnClickListener {
            replaceFragment(HomePage.newInstance(), false)
        }
        imgMore.setOnClickListener {
            val noteBottomSheetFragment = BottomSheet.newInstance(noteId)
            noteBottomSheetFragment.show(
                requireActivity().supportFragmentManager,
                "Note Bottom Sheet Fragment"
            )
        }
    }

    private fun updateNote() {
        launch {
            context?.let {
                if (sql_based) {
                    val note = DatabaseNote.getDatabase(it).noteDao().getSpecificNote(noteId)
                    note.title = NoteTitle.text.toString()
                    note.noteText = Notetext.text.toString()
                    note.dateTime = currentDate + " saved in database"
                    note.color = selectedColor

                    DatabaseNote.getDatabase(it).noteDao().updateNote(note)
                } else {
                    var fileNotes: MutableList<Note?> = mutableListOf()
                    val file = File(it.filesDir, ApplicationState.internalFile)
                    fileNotes = HomePage.newInstance().Deserialize(file, it, fileNotes)
                    fileNotes.removeIf { note -> note?.id == noteId }

                    val fileOutputStream: FileOutputStream = requireContext().openFileOutput(
                        ApplicationState.internalFile,
                        Context.MODE_PRIVATE
                    )
                    for (note in fileNotes) {
                        SerializeAndWrite(note!!, fileOutputStream)
                    }
                    saveNote()

                }
                replaceFragment(HomePage.newInstance(), false)
            }
        }
    }

    private fun saveNote() {
        isnote = true
        istitle = true
        if (NoteTitle.text.isNullOrEmpty()) {
            Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
            istitle = false
        }
        if (Notetext.text.isNullOrEmpty()) {
            Toast.makeText(context, "Note Required", Toast.LENGTH_SHORT).show()
            isnote = false
        }
        if (isnote && istitle) {
            launch(Dispatchers.IO) {
                val note = Note()
                note.title = NoteTitle.text.toString()
                note.noteText = Notetext.text.toString()
                note.color = selectedColor

                context?.let {
                    if (ApplicationState.mode) {
                        note.dateTime = currentDate + " saved in database"
                        val noteId: Long = DatabaseNote.getDatabase(it).noteDao().insertNote(note)
                        Log.d("NotesApp", "Note saved with id=$noteId")
                    } else {
                        note.dateTime = currentDate + " saved in file"
                        note.id = currentTimeMillis()
                        note.sqlBased = false
                        val fileOutputStream: FileOutputStream = requireContext().openFileOutput(
                            ApplicationState.internalFile,
                            Context.MODE_APPEND
                        )
                        val json: String = SerializeAndWrite(note, fileOutputStream)

                        Log.d("NotesApp", "Note saved in file: $json")
                        Log.d("NotesApp", "Note saved in file: $json")
                    }
                    replaceFragment(HomePage.newInstance(), false)
                }
            }
        }
    }

    fun SerializeAndWrite(note: Note, fileOutputStream: FileOutputStream): String {
        val json: String = Json.myJsonEncode(note)


        val outputStreamWriter = OutputStreamWriter(fileOutputStream)
        outputStreamWriter.write(json + "\n")
        outputStreamWriter.close()
        return json
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
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1!!.getStringExtra("action")
            when (action!!) {
                "ChangeColor" -> {
                    selectedColor = p1.getStringExtra("selectedColor")!!
                }
            }
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }


}