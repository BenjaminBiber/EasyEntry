package com.easyentry.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.ui.graphics.vector.ImageVector

enum class GroupIcon {
    HOME,
    GARAGE,
    LOCK,
    STAR,
    CAR,
    APARTMENT,
    BUILD,
    MEETING_ROOM,
    KEY,
    SECURITY,
    SHIELD,
    WIFI,
    PERSON,
    BUSINESS,
    LOCAL_PARKING,
    STOREFRONT,
    WORK,
    SCHOOL,
    LOCATION_ON,
    FAVORITE,
    BOLT,
    SETTINGS,
    // Landwirtschaft & Natur
    AGRICULTURE,
    GRASS,
    FOREST,
    PARK,
    ECO,
    YARD,
    TERRAIN,
    WATER_DROP,
    PETS;

    fun toImageVector(): ImageVector = when (this) {
        HOME -> Icons.Default.Home
        GARAGE -> Icons.Default.Garage
        LOCK -> Icons.Default.Lock
        STAR -> Icons.Default.Star
        CAR -> Icons.Default.DirectionsCar
        APARTMENT -> Icons.Default.Apartment
        BUILD -> Icons.Default.Build
        MEETING_ROOM -> Icons.Default.MeetingRoom
        KEY -> Icons.Default.Key
        SECURITY -> Icons.Default.Security
        SHIELD -> Icons.Default.Shield
        WIFI -> Icons.Default.Wifi
        PERSON -> Icons.Default.Person
        BUSINESS -> Icons.Default.Business
        LOCAL_PARKING -> Icons.Default.LocalParking
        STOREFRONT -> Icons.Default.Storefront
        WORK -> Icons.Default.Work
        SCHOOL -> Icons.Default.School
        LOCATION_ON -> Icons.Default.LocationOn
        FAVORITE -> Icons.Default.Favorite
        BOLT -> Icons.Default.Bolt
        SETTINGS -> Icons.Default.Settings
        AGRICULTURE -> Icons.Default.Agriculture
        GRASS -> Icons.Default.Grass
        FOREST -> Icons.Default.Forest
        PARK -> Icons.Default.Park
        ECO -> Icons.Default.Eco
        YARD -> Icons.Default.Yard
        TERRAIN -> Icons.Default.Terrain
        WATER_DROP -> Icons.Default.WaterDrop
        PETS -> Icons.Default.Pets
    }
}
