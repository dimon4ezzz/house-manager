package com.dvor.my.mydvor.data

/**
 * Presents retailer on the street with sales list.
 */
data class Retailer(
        val id: String,
        val stocks: List<Stock>?
)