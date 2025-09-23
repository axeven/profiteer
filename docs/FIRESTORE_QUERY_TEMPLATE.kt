// FIRESTORE SECURITY TEMPLATE
// Copy this template when creating new Firestore queries to ensure security compliance

/**
 * Template for secure Firestore queries in Profiteer app
 *
 * 🚨 SECURITY REQUIREMENT: All queries MUST include userId filter first
 * See: docs/FIREBASE_SECURITY_GUIDELINES.md for complete requirements
 */

// ✅ SECURE QUERY TEMPLATE
fun getSecureData(
    dataId: String,
    userId: String  // ← REQUIRED: Always accept userId parameter
): Flow<List<SomeModel>> = callbackFlow {

    val listener = firestore.collection("collection_name")
        .whereEqualTo("userId", userId)           // ← REQUIRED: Always filter by userId FIRST
        .whereEqualTo("dataField", dataId)        // ← Additional filters after userId
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                logger.e("Repository", "Error in query", error)
                // Handle error appropriately
                return@addSnapshotListener
            }

            val results = snapshot?.documents?.mapNotNull { document ->
                try {
                    document.toObject(SomeModel::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    logger.e("Repository", "Error parsing document: ${document.id}", e)
                    null
                }
            }?.filter { it.id.isNotEmpty() } ?: emptyList()

            trySend(results)
        }

    awaitClose { listener.remove() }
}

// ✅ SECURE ARRAY QUERY TEMPLATE
fun getSecureArrayData(
    arrayValue: String,
    userId: String  // ← REQUIRED: Always accept userId parameter
): Flow<List<SomeModel>> = callbackFlow {

    val listener = firestore.collection("collection_name")
        .whereEqualTo("userId", userId)           // ← REQUIRED: userId filter FIRST
        .whereArrayContains("arrayField", arrayValue)  // ← Array query after userId
        .addSnapshotListener { snapshot, error ->
            // ... same error handling pattern
        }

    awaitClose { listener.remove() }
}

// ✅ SECURE COMPOSITE QUERY TEMPLATE
fun getSecureCompositeData(
    field1: String,
    field2: String,
    userId: String  // ← REQUIRED: Always accept userId parameter
): Flow<List<SomeModel>> = callbackFlow {

    val listener = firestore.collection("collection_name")
        .whereEqualTo("userId", userId)           // ← REQUIRED: userId filter FIRST
        .whereEqualTo("field1", field1)           // ← Additional filters after userId
        .whereEqualTo("field2", field2)           // ← More filters as needed
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            // ... same error handling pattern
        }

    awaitClose { listener.remove() }
}

// ✅ SECURE ONE-TIME QUERY TEMPLATE
suspend fun getSecureDataOnce(
    dataId: String,
    userId: String  // ← REQUIRED: Always accept userId parameter
): Result<List<SomeModel>> {
    return try {
        val snapshot = firestore.collection("collection_name")
            .whereEqualTo("userId", userId)       // ← REQUIRED: userId filter FIRST
            .whereEqualTo("dataField", dataId)    // ← Additional filters after userId
            .get()
            .await()

        val results = snapshot.documents.mapNotNull { document ->
            try {
                document.toObject(SomeModel::class.java)?.copy(id = document.id)
            } catch (e: Exception) {
                logger.e("Repository", "Error parsing document: ${document.id}", e)
                null
            }
        }.filter { it.id.isNotEmpty() }

        Result.success(results)
    } catch (e: Exception) {
        logger.e("Repository", "Error in query", e)
        Result.failure(e)
    }
}

// ❌ FORBIDDEN PATTERNS - DO NOT USE
/*
// Missing userId parameter
fun getInsecureData(dataId: String): Flow<List<SomeModel>>

// Missing userId filter
firestore.collection("collection")
    .whereEqualTo("dataField", dataId)  // NO userId filter!
    .get()

// Wrong order (userId should be first)
firestore.collection("collection")
    .whereEqualTo("dataField", dataId)
    .whereEqualTo("userId", userId)     // Should be first!
    .get()
*/

// 📝 USAGE IN VIEWMODEL
/*
class SomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    fun loadData(dataId: String) {
        viewModelScope.launch {
            dataRepository.getSecureData(dataId, userId)  // ← Pass userId
                .collect { data ->
                    // Handle data
                }
        }
    }
}
*/