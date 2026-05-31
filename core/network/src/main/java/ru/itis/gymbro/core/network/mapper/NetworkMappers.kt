package ru.itis.gymbro.core.network.mapper

import ru.itis.gymbro.core.domain.model.*
import ru.itis.gymbro.core.network.api.*

fun UserShortResponse.toDomain(): User {
    return User(
        id = id,
        name = name,
        avatarUrl = avatarUrl
    )
}

fun LocationDetailResponse.toDomain(): LocationSpot {
    return LocationSpot(
        id = id,
        name = name,
        description = description,
        type = type,
        latitude = latitude,
        longitude = longitude,
        photoUrls = photoUrls,
        equipmentTags = equipmentTags,
        isVerified = isVerified,
        avgRating = avgRating,
        reviewCount = reviewCount
    )
}

fun LocationSummaryResponse.toDomain(): LocationSpot {
    return LocationSpot(
        id = id,
        name = name,
        type = type,
        latitude = latitude,
        longitude = longitude,
        avgRating = avgRating,
        reviewCount = reviewCount,
        equipmentTags = equipmentTags,
        distance = distance,
        isVerified = verified
    )
}

fun ParticipantResponse.toDomain(): Participant {
    return Participant(
        userId = userId,
        name = userInfo.name,
        avatarUrl = userInfo.avatarUrl,
        status = status,
        joinedAt = joinedAt
    )
}

fun EventDetailResponse.toDomain(): WorkoutEvent {
    return WorkoutEvent(
        id = id,
        title = title,
        description = description,
        workoutType = workoutType,
        wishes = wishes,
        startTime = startTime,
        endTime = endTime,
        maxParticipants = maxParticipants,
        status = status,
        host = host.toDomain(),
        participants = participants.map { it.toDomain() },
        location = location.toDomain()
    )
}

fun EventSummaryResponse.toDomain(): WorkoutEvent {
    return WorkoutEvent(
        id = id,
        title = title,
        workoutType = workoutType,
        startTime = startTime,
        endTime = endTime,
        maxParticipants = maxParticipants,
        status = status,
        host = host.toDomain(),
        participants = emptyList(),
        location = location.toDomain(),
        distance = distance
    )
}

fun ReviewResponse.toDomain(): Review {
    return Review(
        id = id,
        rating = rating,
        comment = comment,
        tags = tags,
        createdAt = createdAt,
        author = author.toDomain()
    )
}
