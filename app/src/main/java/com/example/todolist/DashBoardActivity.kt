package com.example.todolist

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.DTO.ToDo
import kotlinx.android.synthetic.main.activity_dash_board.*

class DashBoardActivity : Activity() {

    lateinit var context : Context
    lateinit var dbHandler : DBHandler
    lateinit var toolbar : Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        context = applicationContext

        rv_dashboard.layoutManager = LinearLayoutManager(this)

        val mDividerItemDecoration = DividerItemDecoration(
            rv_dashboard.context,
            DividerItemDecoration.HORIZONTAL
        )

        rv_dashboard.addItemDecoration(mDividerItemDecoration)


        dbHandler = DBHandler(this)
        rv_dashboard.layoutManager = LinearLayoutManager(this )
        fab_dashboard.setOnClickListener {
            val dialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val toDoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setTitle("Add ToDo")
            dialog.setPositiveButton("Add", { _: DialogInterface, _: Int ->
                if(toDoName.text.isNotEmpty() ){
                    val toDo = ToDo()
                    toDo.name = toDoName.text.toString()
                    dbHandler.addToDo(toDo)
                    refreshList()
                }
            })
            dialog.setNegativeButton("Cancel" ) { _: DialogInterface, _: Int ->

            }
            dialog.show()

        }
    }

    fun updateToDo(toDo: ToDo){
        val dialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val toDoName = view.findViewById<EditText>(R.id.ev_todo)
        toDoName.setText(toDo.name)
        dialog.setView(view)
        dialog.setTitle("Update ToDo")
        dialog.setPositiveButton("Update", { _: DialogInterface, _: Int ->
            if(toDoName.text.isNotEmpty() ){
                toDo.name = toDoName.text.toString()
                dbHandler.updateToDo(toDo)
                refreshList()
            }
        })
        dialog.setNegativeButton("Cancel" ) { _: DialogInterface, _: Int ->

        }
        dialog.show()
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList(){
        dbHandler = DBHandler(context)
        rv_dashboard.adapter = DashBoardAdapter(this, dbHandler.getToDos())
    }

    class DashBoardAdapter( val activity: DashBoardActivity, val list: MutableList<ToDo>) : RecyclerView.Adapter<DashBoardAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_child_dashboard, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.toDoName.text = list[position].name

            holder.toDoName.setOnClickListener {
                val intent = Intent(activity, ItemActivity::class.java)
                intent.putExtra(INTENT_TODO_ID, list[position].id )
                intent.putExtra(INTENT_TODO_NAME, list[position].name )
                activity.startActivity(intent)
            }

            holder.menu.setOnClickListener {
                val popup = PopupMenu(activity, holder.menu)
                popup.inflate(R.menu.dashboard_child)

                popup.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.menu_edit->{
                            activity.updateToDo(list[position])
                        }
                        R.id.menu_delete -> {
                            val dialog = AlertDialog.Builder(activity, R.style.MyDialogTheme)
                            dialog.setTitle("Are you sure")
                            dialog.setMessage("Do you want delete this task ?")
                            dialog.setPositiveButton("Continue", { _: DialogInterface, _: Int ->
                                activity.dbHandler.deleteToDo(list[position].id)
                                activity.refreshList()
                            })
                            dialog.setNegativeButton("Cancel" ) { _: DialogInterface, _: Int ->
                            }
                            dialog.show()
                        }
                        R.id.menu_mark_as_completed->{
                            activity.dbHandler.updateToDoItemCompleteStatus(list[position].id, true)
                        }
                        R.id.menu_reset->{
                            activity.dbHandler.updateToDoItemCompleteStatus(list[position].id, false)

                        }
                    }
                    true
                }
                popup.show()
            }
        }
        class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val toDoName : TextView = v.findViewById(R.id.tv_todo_name)
            val menu : ImageView = v.findViewById(R.id.iv_menu)

        }
    }
}
