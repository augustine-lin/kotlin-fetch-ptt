package com.example.models


data class Customer(val id: String, val name: String, val phone: String)
val customerStorage = mutableListOf<Customer>()
