package com.example.todolist

import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.DTO.ToDoItem
import kotlinx.android.synthetic.main.activity_item.*
import java.util.*

class ItemActivity : AppCompatActivity() {

    lateinit var dbHandler : DBHandler
    var toDoId : Long = -1
    var list : MutableList<ToDoItem>? = null
    var adapter : ItemAdapter? = null
    var touchHelper : ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        item_toolbar.title = intent.getStringExtra(INTENT_TODO_NAME)

        item_toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        toDoId = intent.getLongExtra(INTENT_TODO_ID, -1)
        dbHandler = DBHandler(this)
        rv_item.layoutManager = LinearLayoutManager(this)


        fab_item.setOnClickListener {
            val dialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val toDoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setTitle("Add Sub Task")
            dialog.setPositiveButton("Add", { _: DialogInterface, _: Int ->
                if(toDoName.text.isNotEmpty() ){
                    val item = ToDoItem()
                    item.itemName = toDoName.text.toString()
                    item.toDoId = toDoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item)
                    refreshList()
                }
            })
            dialog.setNegativeButton("Cancel" ) { _: DialogInterface, _: Int ->
            }
            dialog.show()
        }

        touchHelper = ItemTouchHelper( object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0 ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = viewHolder.adapterPosition
                val targetPosition = target.adapterPosition
                Collections.swap(list, sourcePosition, targetPosition)
                adapter?.notifyItemMoved(sourcePosition, targetPosition)
                return true;
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })

        touchHelper?.attachToRecyclerView(rv_item)
    }

    fun updateItem(item: ToDoItem) {
        val dialog = AlertDialog.Builder(this, R.style.MyDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val toDoName = view.findViewById<EditText>(R.id.ev_todo)
        toDoName.setText(item.itemName)
        dialog.setView(view)
        dialog.setTitle("Update Sub Task")
        dialog.setPositiveButton("Update", { _: DialogInterface, _: Int ->
            if(toDoName.text.isNotEmpty() ){
                item.itemName = toDoName.text.toString()
                item.toDoId = toDoId
                item.isCompleted = false
                dbHandler.updateToDoItem(item)
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
        list = dbHandler.getToDoItems(toDoId)
        adapter = ItemAdapter(this, list!!)
        rv_item.adapter = adapter
    }

    class ItemAdapter(val activity: ItemActivity, val list: MutableList<ToDoItem>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_child_item, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemName.text = list[position].itemName
            holder.itemName.isChecked = list[position].isCompleted

            holder.itemName.setOnClickListener {
                list[position].isCompleted = !list[position].isCompleted
                activity.dbHandler.updateToDoItem(list[position])
            }

            holder.delete.setOnClickListener {
                val dialog = AlertDialog.Builder(activity, R.style.MyDialogTheme)
                dialog.setTitle("Are you sure")
                dialog.setMessage("Do you want delete this item ?")
                dialog.setPositiveButton("Continue", { _: DialogInterface, _: Int ->
                    activity.dbHandler.deleteToDoItem(list[position].id)
                    activity.refreshList()
                })
                dialog.setNegativeButton("Cancel" ) { _: DialogInterface, _: Int ->
                }
                dialog.show()

            }

            holder.edit.setOnClickListener {
                activity.updateItem(list[position])
            }

            holder.move.setOnTouchListener { view, motionEvent ->
                if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN){
                    activity.touchHelper?.startDrag(holder)
                }
                false
            }
        }

        class ViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val itemName : CheckBox = v.findViewById(R.id.cb_item)
            val edit : ImageView = v.findViewById(R.id.iv_edit)
            val delete : ImageView = v.findViewById(R.id.iv_delete)
            val move : ImageView = v.findViewById(R.id.iv_drag_and_drop)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home ) {
            finish()
            true
        } else
            super.onOptionsItemSelected(item)
    }
}
