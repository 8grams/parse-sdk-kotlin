package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import kotlin.math.*

/**
 * GeoPoint class for handling geographic coordinates
 */
@Serializable
class ParseGeoPoint : Encodable {
    var latitude: Double = 0.0
        private set

    var longitude: Double = 0.0
        private set

    constructor(latitude: Double, longitude: Double) {
        setLatitude(latitude)
        setLongitude(longitude)
    }

    /**
     * Set the latitude
     * @throws IllegalArgumentException if latitude is not between -90 and 90
     */
    fun setLatitude(latitude: Double) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw IllegalArgumentException("Latitude must be between -90 and 90")
        }
        this.latitude = latitude
    }

    /**
     * Set the longitude
     * @throws IllegalArgumentException if longitude is not between -180 and 180
     */
    fun setLongitude(longitude: Double) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw IllegalArgumentException("Longitude must be between -180 and 180")
        }
        this.longitude = longitude
    }

    /**
     * Calculate the distance in kilometers to another GeoPoint
     */
    fun distanceInKilometersTo(point: ParseGeoPoint): Double {
        return distanceInRadiansTo(point) * 6371.0 // Earth's radius in kilometers
    }

    /**
     * Calculate the distance in miles to another GeoPoint
     */
    fun distanceInMilesTo(point: ParseGeoPoint): Double {
        return distanceInRadiansTo(point) * 3958.8 // Earth's radius in miles
    }

    /**
     * Calculate the distance in radians to another GeoPoint
     */
    fun distanceInRadiansTo(point: ParseGeoPoint): Double {
        val lat1 = latitude * PI / 180.0
        val lon1 = longitude * PI / 180.0
        val lat2 = point.latitude * PI / 180.0
        val lon2 = point.longitude * PI / 180.0

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return c
    }

    /**
     * Check if this GeoPoint is equal to another object
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParseGeoPoint) return false

        return latitude == other.latitude && longitude == other.longitude
    }

    /**
     * Get the hash code of this GeoPoint
     */
    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        return result
    }

    /**
     * Get the string representation of this GeoPoint
     */
    override fun toString(): String {
        return "ParseGeoPoint(latitude=$latitude, longitude=$longitude)"
    }

    /**
     * Encode this GeoPoint for JSON serialization
     */
    override fun encode(): Map<String, Any?> {
        return mapOf(
            "__type" to "GeoPoint",
            "latitude" to latitude,
            "longitude" to longitude
        )
    }

    companion object {
        /**
         * Create a GeoPoint from a map
         */
        fun fromMap(map: Map<String, Any?>): ParseGeoPoint {
            val latitude = map["latitude"] as? Double ?: throw IllegalArgumentException("Latitude is required")
            val longitude = map["longitude"] as? Double ?: throw IllegalArgumentException("Longitude is required")
            return ParseGeoPoint(latitude, longitude)
        }
    }
}