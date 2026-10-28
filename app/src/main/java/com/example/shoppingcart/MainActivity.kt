package com.example.shoppingcart

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shoppingcart.databinding.ActivityMainBinding

data class ShoppingItem(
    val id: String,
    val productName: String,
    val quantity: String,
    val category: String,
    val unit: String,
    var isBought: Boolean
) : Parcelable {

    constructor(parcel: Parcel): this(
        id = parcel.readString() ?: "",
        productName = parcel.readString() ?: "",
        quantity = parcel.readString() ?: "",
        category = parcel.readString() ?: "",
        unit = parcel.readString() ?: "",
        isBought = parcel.readBoolean()
    )
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(productName)
        dest.writeString(quantity)
        dest.writeString(category)
        dest.writeString(unit)
        dest.writeBoolean(isBought)
        dest.writeBoolean(isBought)
    }
    companion object CREATOR : Parcelable.Creator<ShoppingItem> {
        override fun createFromParcel(parcel: Parcel): ShoppingItem {
            return ShoppingItem(parcel)
        }

        override fun newArray(size: Int): Array<ShoppingItem?> {
            return arrayOfNulls(size)
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val SHOPPING_ITEM = "ShippingItem"
    private lateinit var shoppingAdapter: ShoppingAdapter

    private val shoppingList = ArrayList<ShoppingItem>()

    private companion object {
        const val KEY_SHOPPING_LIST = "key_shopping_list"
    }

    // Activity A (data ) -> Activity B
    private val addItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),

    ){ result ->
        if(result.resultCode == RESULT_OK && result.data != null){
            val newItem = result.data?.getParcelableExtra<ShoppingItem>("EXTRA_SHOPPING_ITEM")
            if(newItem != null){
                shoppingList.add(newItem)
                shoppingAdapter.notifyItemInserted(shoppingList.size -1)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val basePadding = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + basePadding,
                systemBars.top + basePadding,
                systemBars.right + basePadding,
                systemBars.bottom + basePadding
            )
            insets
        }
        setClickListeners()


        if(savedInstanceState != null){
            val restoredList = savedInstanceState.getParcelableArrayList<ShoppingItem>(KEY_SHOPPING_LIST)
            if (restoredList != null) {
                shoppingList.clear()
                shoppingList.addAll(restoredList)
            }
        }

        shoppingAdapter = ShoppingAdapter(shoppingList) { item, position, isChecked ->

            item.isBought = isChecked
            shoppingAdapter.notifyItemChanged(position)
        }

        binding.rvShoppingList.adapter = shoppingAdapter
        binding.rvShoppingList.layoutManager = LinearLayoutManager(this)

    }

    private fun updateCartVisibility(){
        if (shoppingList.isNotEmpty()) {
            binding.rvShoppingList.visibility = View.VISIBLE
            binding.llEmptyCart.visibility = View.GONE
            binding.FABAddItem.visibility = View.VISIBLE
        } else {
            binding.rvShoppingList.visibility = View.GONE
            binding.llEmptyCart.visibility = View.VISIBLE
            binding.FABAddItem.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(KEY_SHOPPING_LIST, shoppingList)
    }

    fun setClickListeners() {
        binding.btnAddFirstItem.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            addItemLauncher.launch(intent)
        }
        binding.FABAddItem.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            addItemLauncher.launch(intent)
        }
    }
}

class ShoppingAdapter(
    private val shoppingList: List<ShoppingItem>,
    private val onCheckedChangeListener: (item: ShoppingItem, position: Int, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<ShoppingAdapter.ShoppingViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ShoppingViewHolder {
        return ShoppingViewHolder(
            itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.shopping_list_item, parent, false),
            onCheckedChangeListener = onCheckedChangeListener
        )
    }

    override fun onBindViewHolder(
        holder: ShoppingViewHolder,
        position: Int
    ) {
        val currentItem = shoppingList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return shoppingList.size
    }

    class ShoppingViewHolder(
        private val itemView: View,
        private val onCheckedChangeListener: (item: ShoppingItem, position: Int, isChecked: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val category: TextView = itemView.findViewById(R.id.tv_category)
        private val quantity: TextView = itemView.findViewById(R.id.tv_quantity)
        private val unit: TextView = itemView.findViewById(R.id.tv_unit)
        private val status: TextView = itemView.findViewById(R.id.tv_status)
        private val checkBox: CheckBox = itemView.findViewById(R.id.cb_item)

        fun bind(item: ShoppingItem) {
            
            itemName.text = item.productName
            category.text = item.category
            quantity.text = item.quantity
            unit.text = item.unit
            checkBox.isChecked = item.isBought

            updateVisuals(item.isBought)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChangeListener(item, adapterPosition, isChecked)
            }
        }

        private fun updateVisuals(isBought: Boolean) {
            if (isBought) {
                itemName.paintFlags = itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                category.paintFlags = category.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                quantity.paintFlags = quantity.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                unit.paintFlags = unit.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                status.text = "Bought"
            } else {
                itemName.paintFlags = itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                category.paintFlags = category.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                quantity.paintFlags = quantity.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                unit.paintFlags = unit.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                status.text = "Pending"
            }
        }
    }
}
