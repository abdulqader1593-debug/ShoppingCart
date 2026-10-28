package com.example.shoppingcart

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shoppingcart.databinding.ActivityAddItemBinding
import com.google.android.material.chip.Chip

class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemBinding
    private var itemName = ""
    private var itemQuantity = 0
    private val newAddedItem = "EXTRA_SHOPPING_ITEM"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListeners()
        setInputListeners()
        val basePadding = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left + basePadding, systemBars.top + basePadding, systemBars.right + basePadding, systemBars.bottom +basePadding)
            insets
        }
    }

    private fun validateForm(){
        itemName = binding.textinputItemName.editText?.text.toString().trim()
        itemQuantity = binding.textinputQuantity.editText?.text.toString().toIntOrNull() ?: 0

        if(itemName.isNotEmpty()){
            binding.textinputItemName.error = null
        } else {
            binding.textinputItemName.error = "Item name cannot be empty"
        }

        if(itemQuantity > 0){
            binding.textinputQuantity.error = null
        } else {
            binding.textinputQuantity.error = "Quantity should be greater than 0"
        }

        val isCategorySelected = binding.cgCategory.checkedChipId != View.NO_ID
        val isFormValid = itemName.isNotEmpty() && itemQuantity > 0 && isCategorySelected
        binding.btnAddToList.isEnabled = isFormValid
    }

    private fun setInputListeners(){
        binding.textinputItemName.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
        })
        binding.textinputQuantity.editText?.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
        })

        binding.cgCategory.setOnCheckedStateChangeListener { _, _ ->
            validateForm()
        }
    }

    private fun setClickListeners(){
        binding.btnCancel.setOnClickListener {
           finish()
        }
        binding.btnAddToList.setOnClickListener {
            val selectedChipId = binding.cgCategory.checkedChipId
            val category = if (selectedChipId != View.NO_ID) {
                findViewById<Chip>(selectedChipId).text.toString()
            } else {
                "Pantry"
            }
            val newItem = ShoppingItem(
                id= System.currentTimeMillis().toString(),
                productName = itemName,
                quantity = itemQuantity.toString(),
                category = category,
                unit = "Kg",
                isBought = false
            )
            val resultIntent = Intent().apply {
                putExtra(newAddedItem,newItem)
            }
            setResult(RESULT_OK,resultIntent)
            finish()
        }
    }
}