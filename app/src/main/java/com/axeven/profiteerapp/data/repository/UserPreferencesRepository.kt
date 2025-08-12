package com.axeven.profiteerapp.data.repository

import com.axeven.profiteerapp.data.model.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val preferencesCollection = firestore.collection("user_preferences")

    fun getUserPreferences(userId: String): Flow<UserPreferences?> = callbackFlow {
        val listener = preferencesCollection
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val preferences = snapshot?.documents?.firstOrNull()?.toObject(UserPreferences::class.java)
                trySend(preferences)
            }
        
        awaitClose { listener.remove() }
    }

    suspend fun createOrUpdatePreferences(preferences: UserPreferences): Result<String> {
        return try {
            // Check if preferences already exist
            val existingSnapshot = preferencesCollection
                .whereEqualTo("userId", preferences.userId)
                .limit(1)
                .get()
                .await()

            if (existingSnapshot.documents.isNotEmpty()) {
                // Update existing preferences
                val docId = existingSnapshot.documents.first().id
                preferencesCollection.document(docId).set(preferences).await()
                Result.success(docId)
            } else {
                // Create new preferences
                val documentRef = preferencesCollection.add(preferences).await()
                Result.success(documentRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDefaultCurrency(userId: String, currency: String): Result<Unit> {
        return try {
            val snapshot = preferencesCollection
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val docId = snapshot.documents.first().id
                preferencesCollection.document(docId)
                    .update("defaultCurrency", currency)
                    .await()
            } else {
                // Create new preferences if none exist
                val newPreferences = UserPreferences(
                    userId = userId,
                    defaultCurrency = currency
                )
                preferencesCollection.add(newPreferences).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}