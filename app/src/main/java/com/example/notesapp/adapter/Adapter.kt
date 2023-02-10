package com.example.notesapp.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.R
import com.example.notesapp.entities.Note
import kotlinx.android.synthetic.main.fragment_home_page.*
import kotlinx.android.synthetic.main.items_of_notes.view.*

class Adapter() : RecyclerView.Adapter<Adapter.NotesHolder>() {
    var listener: OnItemClickListener? = null
    var arrList: ArrayList<Note?> = ArrayList()
    var deleted: Boolean = false
    var count = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesHolder {
        return NotesHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.items_of_notes, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    fun setData(arrNoteList: List<Note?>) {
        arrList.clear()
        arrList.addAll(arrNoteList)
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        holder.itemView.Title.text = arrList[position]?.title
        holder.itemView.Desc.text = arrList[position]?.noteText
        holder.itemView.tvDateTime.text = arrList[position]?.dateTime
        holder.itemView.cardView.setOnClickListener {
            listener!!.onClicked(arrList[position]?.id!!, arrList[position]?.sqlBased!!)
            count = 0
        }
        if (arrList[position]?.color != null) {
            holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(arrList[position]?.color))
        } else {
            //holder.itemView.cardView.setCardBackgroundColor(Color.parseColor(R.color.ColorLightBlack.toString()))
        }

        holder.itemView.icDelete.setOnClickListener {

            val noteId = arrList[holder.layoutPosition]?.id!!
            val sql_based = arrList[holder.layoutPosition]?.sqlBased!!

            count++
            arrList.removeAt(holder.layoutPosition)
            notifyItemRemoved(holder.layoutPosition)

//            runBlocking {
//                coroutineScope {
//                    launch(Dispatchers.IO) {
//                        Log.d("NotesApp", "Deleting note with id=$noteId ...")
//                        DatabaseNote.getDatabase(it.context).noteDao().deleteSpecificNote(noteId)
//                        // TODO temporary, remove later
//                        val notes = DatabaseNote.getDatabase(it.context).noteDao().getAllNotes()
//                        Log.d("NotesApp", Json.myJsonEncode(notes))
//                    }
//                }
//            }

            val intent = Intent("action_del")
            intent.putExtra("action", "DeleteNote")
            intent.putExtra("noteId", noteId)
            intent.putExtra("sql_based", sql_based)
            LocalBroadcastManager.getInstance(holder.itemView.context).sendBroadcast(intent)

            deleted = true
        }
    }


    class NotesHolder(val view: View) : RecyclerView.ViewHolder(view) {
    }

    interface OnItemClickListener {
        fun onClicked(noteId: Long, sql_based: Boolean)
    }
}
