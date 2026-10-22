package com.example.lankago.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.lankago.data.ActivityItem
import com.example.lankago.data.RentalVehicle
import com.example.lankago.data.Trip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TripManager {

    private const val PREF_NAME = "lankago_trip_planner_pref"
    private const val KEY_TRIPS = "key_saved_trips_json"
    private val gson = Gson()

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves all saved trips from SharedPreferences.
     */
    fun getAllTrips(context: Context): List<Trip> {
        val json = getPreferences(context).getString(KEY_TRIPS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Trip>>() {}.type
            gson.fromJson<List<Trip>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Finds a saved trip by its unique ID.
     */
    fun getTripById(context: Context, tripId: String): Trip? {
        return getAllTrips(context).find { it.id == tripId }
    }

    /**
     * Saves a new trip or updates an existing trip if the ID already matches.
     */
    fun saveTrip(context: Context, trip: Trip) {
        val currentTrips = getAllTrips(context).toMutableList()
        val existingIndex = currentTrips.indexOfFirst { it.id == trip.id }

        if (existingIndex != -1) {
            currentTrips[existingIndex] = trip
        } else {
            currentTrips.add(0, trip)
        }

        persistTrips(context, currentTrips)
    }

    /**
     * Deletes a trip by its ID.
     */
    fun deleteTrip(context: Context, tripId: String) {
        val currentTrips = getAllTrips(context).toMutableList()
        currentTrips.removeAll { it.id == tripId }
        persistTrips(context, currentTrips)
    }

    /**
     * Adds a Place to a specific Trip's places list.
     */
    fun addPlaceToTrip(context: Context, tripId: String, place: Place): Boolean {
        val currentTrips = getAllTrips(context).toMutableList()
        val tripIndex = currentTrips.indexOfFirst { it.id == tripId }

        if (tripIndex != -1) {
            val targetTrip = currentTrips[tripIndex]
            val alreadyContains = targetTrip.places.any { it.id == place.id }

            if (!alreadyContains) {
                targetTrip.places.add(place)
                persistTrips(context, currentTrips)
                return true
            }
        }
        return false
    }

    /**
     * Appends a booked RentalVehicle to the active trip, or creates a new trip if none exists.
     */
    fun addRentalToTrip(context: Context, tripId: String, rental: RentalVehicle): Boolean {
        val currentTrips = getAllTrips(context).toMutableList()
        val tripIndex = if (tripId.isNotEmpty()) currentTrips.indexOfFirst { it.id == tripId } else if (currentTrips.isNotEmpty()) 0 else -1

        if (tripIndex != -1 && tripIndex < currentTrips.size) {
            val targetTrip = currentTrips[tripIndex]
            targetTrip.bookedRentals.add(rental)
            persistTrips(context, currentTrips)
            return true
        } else {
            // Edge-Case Handling: Auto-create active trip so booking is never lost
            val autoTrip = Trip(
                id = if (tripId.isNotBlank()) tripId else "trip_${System.currentTimeMillis()}",
                tripName = "${rental.location} Adventure",
                date = "Flexible Dates",
                travelerType = "Local (NIC)",
                idImageUri = null,
                places = mutableListOf(),
                bookedRentals = mutableListOf(rental),
                bookedActivities = mutableListOf()
            )
            currentTrips.add(0, autoTrip)
            persistTrips(context, currentTrips)
            return true
        }
    }

    /**
     * Appends a booked ActivityItem to the active trip, or creates a new trip if none exists.
     */
    fun addActivityToTrip(context: Context, tripId: String, activity: ActivityItem): Boolean {
        val currentTrips = getAllTrips(context).toMutableList()
        val tripIndex = if (tripId.isNotEmpty()) currentTrips.indexOfFirst { it.id == tripId } else if (currentTrips.isNotEmpty()) 0 else -1

        if (tripIndex != -1 && tripIndex < currentTrips.size) {
            val targetTrip = currentTrips[tripIndex]
            targetTrip.bookedActivities.add(activity)
            persistTrips(context, currentTrips)
            return true
        } else {
            // Edge-Case Handling: Auto-create active trip so booking is never lost
            val autoTrip = Trip(
                id = if (tripId.isNotBlank()) tripId else "trip_${System.currentTimeMillis()}",
                tripName = "${activity.location} Adventure",
                date = "Flexible Dates",
                travelerType = "Local (NIC)",
                idImageUri = null,
                places = mutableListOf(),
                bookedRentals = mutableListOf(),
                bookedActivities = mutableListOf(activity)
            )
            currentTrips.add(0, autoTrip)
            persistTrips(context, currentTrips)
            return true
        }
    }

    private fun persistTrips(context: Context, trips: List<Trip>) {
        val json = gson.toJson(trips)
        getPreferences(context).edit().putString(KEY_TRIPS, json).apply()
    }
}
