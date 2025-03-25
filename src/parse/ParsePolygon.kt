package parse

import parse.internal.Encodable
import kotlinx.serialization.Serializable
import kotlin.math.*

/**
 * Polygon class for handling polygon shapes
 */
@Serializable
class ParsePolygon : Encodable {
    private val coordinates: MutableList<ParseGeoPoint> = mutableListOf()

    constructor(points: List<ParseGeoPoint>) {
        if (points.size < 3) {
            throw IllegalArgumentException("A polygon must have at least 3 points")
        }
        coordinates.addAll(points)
    }

    /**
     * Get the list of coordinates that make up this polygon
     */
    fun getCoordinates(): List<ParseGeoPoint> = coordinates.toList()

    /**
     * Check if a point is inside this polygon
     */
    fun containsPoint(point: ParseGeoPoint): Boolean {
        var inside = false
        var j = coordinates.size - 1

        for (i in coordinates.indices) {
            if ((coordinates[i].longitude > point.longitude) != (coordinates[j].longitude > point.longitude) &&
                point.latitude < (coordinates[j].latitude - coordinates[i].latitude) * (point.longitude - coordinates[i].longitude) /
                (coordinates[j].longitude - coordinates[i].longitude) + coordinates[i].latitude) {
                inside = !inside
            }
            j = i
        }

        return inside
    }

    /**
     * Check if this polygon is equal to another object
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParsePolygon) return false

        return coordinates == other.coordinates
    }

    /**
     * Get the hash code of this polygon
     */
    override fun hashCode(): Int {
        return coordinates.hashCode()
    }

    /**
     * Get the string representation of this polygon
     */
    override fun toString(): String {
        return "ParsePolygon(coordinates=$coordinates)"
    }

    /**
     * Encode this polygon for JSON serialization
     */
    override fun encode(): Map<String, Any?> {
        return mapOf(
            "__type" to "Polygon",
            "coordinates" to coordinates.map { it.encode() }
        )
    }

    companion object {
        /**
         * Create a polygon from a list of coordinates
         */
        fun create(points: List<ParseGeoPoint>): ParsePolygon {
            return ParsePolygon(points)
        }

        /**
         * Create a polygon from a map
         */
        fun fromMap(map: Map<String, Any?>): ParsePolygon {
            val coordinatesList = map["coordinates"] as? List<Map<String, Any?>> 
                ?: throw IllegalArgumentException("Coordinates are required")
            
            val points = coordinatesList.map { ParseGeoPoint.fromMap(it) }
            return ParsePolygon(points)
        }
    }
}