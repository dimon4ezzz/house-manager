package com.dvor.my.mydvor.firebase

import com.dvor.my.mydvor.data.Retailer
import com.dvor.my.mydvor.data.Stock
import com.google.firebase.database.*

/**
 * Support object to get Retailer from Firebase database.
 *
 * Works with `streets/id/retailers` and `retailers` branches.
 */
object RetailersBranchDao {
    /**
     * Constant names in database.
     */
    private const val streets = "streets"
    private const val retailers = "retailers"
    private const val sales = "sales"

    /**
     * Database references.
     */
    private val database = FirebaseDatabase.getInstance().reference
    private var retailersBranch = database.child(retailers)
    private lateinit var streetRetailersBranch: DatabaseReference

    /**
     * Database value listeners.
     */
    private var retailersBranchListener: ValueEventListener? = null
    private var streetRetailersBranchListener: ValueEventListener? = null

    /**
     * Calls `f` function with list of retailers with `streetId`.
     * Detaches last listener from branch.
     *
     * @param streetId id of the street
     * @param f function to call, e.g. updateUI
     * @sample listenRetailersBranch("0") {updateUI(it)}
     * @throws com.google.firebase.database.DatabaseException
     *  when `streets/streetId` or `retailers` is not exist,
     *  or Firebase cancels request
     * @throws IllegalAccessException
     *  when user is not logged in
     */
    fun listenRetailersBranch(streetId: String, f: (retailers: List<Retailer>) -> Unit) {
        stopListenRetailersBranch()
        Auth.checkUserLoginAndThrow()

        streetRetailersBranch = streetRetailersBranch
                .child(streetId)
                .child(retailers)

        streetRetailersBranchListener = object : ValueEventListener {
            // when data changes
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val retailerIds = dataSnapshot.children.map { t ->
                    t.key.toString()
                }
                listenRetailersBranch(retailerIds, f)
            }

            // when Firebase cancels request, throws DatabaseException
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        }

        streetRetailersBranch.addValueEventListener(streetRetailersBranchListener!!)
    }

    /**
     * Собирает данные о магазинах:
     * 1. Проходит по всем `id` магазинов из ветки `street/id`
     * 2. Для текущего `id` из ветки `retailers` берёт ветку `sales`
     * 3. Собирает из ветки `sales` все акции в один лист
     * 4. Создаёт лист из магазинов с указанными акциями
     * 5. Вызывает функцию `f` с этим списком
     *
     * @param retailerIds список `id` магазинов из ветки `street/id`
     * @param f function to call; e.g. updateUI
     * @throws com.google.firebase.database.DatabaseException
     *  когда какая-нибудь ветка не найдена
     */
    private fun listenRetailersBranch(retailerIds: List<String>, f: (retailers: List<Retailer>) -> Unit) {
        retailersBranchListener = object : ValueEventListener {
            // when data changes
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val retailers = retailerIds.map { i ->
                    val stocks = dataSnapshot.child(i).child(sales).children.map { s ->
                        getStock(s)
                    }
                    Retailer(
                            id = i,
                            stocks = stocks
                    )
                }

                f(retailers)
            }

            // when Firebase cancels request, throws DatabaseException
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        }

        retailersBranch.addValueEventListener(retailersBranchListener!!)
    }

    /**
     * Detaches listener from `streets/id/retailers` and `retailers` branches.
     *
     * Set `streets` branch to default path, `listener`s to `null`
     */
    fun stopListenRetailersBranch() {
        retailersBranchListener?.let {
            retailersBranch.removeEventListener(retailersBranchListener!!)
        }

        streetRetailersBranchListener?.let {
            streetRetailersBranch.removeEventListener(streetRetailersBranchListener!!)
        }

        streetRetailersBranch = database.child(streets)

        retailersBranchListener = null
        streetRetailersBranchListener = null
    }

    private fun getStock(dataSnapshot: DataSnapshot): Stock =
            Stock(
                    title = dataSnapshot.child("title").value.toString(),
                    text = dataSnapshot.child("text").value.toString(),
                    address = dataSnapshot.child("address").value.toString(),
                    imgResource = dataSnapshot.child("img").value.toString()
            )
}