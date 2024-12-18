package com.movielist.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.movielist.data.FirebaseTimestampAdapter
import com.movielist.data.UUIDAdapter
import com.movielist.data.FirestoreRepository
import com.movielist.model.ListItem
import com.movielist.model.Movie
import com.movielist.model.Production
import com.movielist.model.TVShow
import com.movielist.model.User
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.log

// ViewModel for å håndtere bruker-logikk
class UserViewModel : ViewModel() {


    private val firestoreRepository = FirestoreRepository(FirebaseFirestore.getInstance())

    private val _loggedInUser = MutableStateFlow<User?>(null)
    val loggedInUser: StateFlow<User?> get() = _loggedInUser

    private val _otherUser = MutableStateFlow<User?>(null)
    val otherUser: StateFlow<User?> get() = _otherUser

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> get() = _searchResults




    suspend fun fetchUsersFromFirebase(query: String): List<User> {

        val usersMaps = firestoreRepository.fetchUsersFromFirebase(query)

        var listOfUsers = mutableListOf<User>()
        if (usersMaps != null) {
            for (userMap in usersMaps) {
                val userObject = convertUserJsonToUserObject(userMap)

                if (userObject != null) {
                    listOfUsers.add(userObject)
                }
            }
        }

        return  listOfUsers.toList()
    }


    fun updateLoggedInUser(updatedUser: User) {
        viewModelScope.launch {
            try {
                _loggedInUser.value = updatedUser
                firestoreRepository.updateUser(updatedUser)

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating logged-in user: ${e.message}")
            }
        }
    }



    fun updateFollowingForUser(updatedUser: User, otherUpdatedUser: User) {
        viewModelScope.launch {
            try {
                _loggedInUser.value = updatedUser
                _otherUser.value = otherUpdatedUser

                firestoreRepository.updateFollowUser(updatedUser,otherUpdatedUser )

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating logged-in user: ${e.message}")
            }
        }
    }


    // Funksjon for å sette User-objekt for innloggede bruker
    fun setLoggedInUser(uid: String) {
        viewModelScope.launch {
            val fetchedUser = getUser(uid) // Kall den suspenderende funksjonen uten callback
            _loggedInUser.value = fetchedUser
        }
    }

    // Funksjon for å sette *ANDRE* brukere - for å vise deres profiler, lister osv.
    fun setOtherUser(uid: String) {
        viewModelScope.launch {
            val fetchedUser = getUser(uid) // Kall den suspenderende funksjonen uten callback
            _otherUser.value = fetchedUser
        }
    }

    suspend fun getUser(userID: String): User? {

        val userJson = firestoreRepository.fetchFirebaseUser(userID)

        return if (userJson != null) {

            convertUserJsonToUserObject(userJson)
        } else {

            Log.w("GetUser", "User not found or failed to fetch user data.")
            null
        }
    }


    suspend fun getUsersFriends(): MutableList<User> {
        val friendsList: MutableList<User> = mutableListOf()

        val friendIDList = loggedInUser.value?.followingList ?: return friendsList

        val totalFriends = friendIDList.size
        var loadedFriends = 0

        for (friendUID in friendIDList) {
            val friend = getUser(friendUID) // Er en suspend funksjon, som vi venter på før vi går videre
            friend?.let {
                friendsList.add(it)
            }
            loadedFriends++
            if (loadedFriends == totalFriends) {
                break
            }
        }

        return friendsList
    }

    fun addOrRemoveFromUsersFavorites(userID: String, listItem: ListItem, isFavorite: Boolean) {
        viewModelScope.launch {

            val user = loggedInUser.value

            when (isFavorite) {
                true -> {

                    if (user != null) {
                        listItem.loggedInUsersFavorite = true
                        val listItemMap = listItem.toMap()

                        val listItemID = listItem.id

                        firestoreRepository.batchUpdateFavoriteStatusAllCollections(
                            userID,
                            listItemID,
                            true,
                            onSuccess = {
                                firestoreRepository.addToFavorites(userID, listItemMap,
                                    onSuccess = {
                                        updateUserObjectCollectionsFavoriteStatus(user, listItemID, true)
                                        user.favoriteCollection.add(listItem)
                                    },
                                    onFailure = { /* Feilhåndtering */ }
                                )
                            },
                            onFailure = { /* Feilhåndtering */ }
                        )
                    }

                }

                false -> {
                    listItem.loggedInUsersFavorite = false

                    firestoreRepository.batchUpdateFavoriteStatusAllCollections(
                        userID,
                        listItem.id,
                        true,
                        onSuccess = {
                            firestoreRepository.removeFromFavorites(userID, listItem,
                                onSuccess = {
                                    user?.favoriteCollection?.remove(listItem)
                                },
                                onFailure = { /* Feilhåndtering */ },
                                onNotFound = { /* Feilhåndtering */ }
                            )
                        },
                        onFailure = { /* Feilhåndtering */ }
                    )
                }
            }
        }
    }

    // TODO : Flytte til intern User klasse metode
    private fun updateUserObjectCollectionsFavoriteStatus(user: User, listItemID: String, isFavorite: Boolean) {
        val collectionsToUpdate = listOf(
            user.completedCollection,
            user.wantToWatchCollection,
            user.currentlyWatchingCollection,
            user.droppedCollection
        )

        collectionsToUpdate.forEach { collection ->

            collection.forEachIndexed { index, item ->
                if (item.id == listItemID) {

                    collection[index] = item.copy(loggedInUsersFavorite = isFavorite)
                    Log.d("Firestore", collection[index].loggedInUsersFavorite.toString())
                }
            }
        }
    }

    fun addOrMoveToUsersCollection(
        userID: String,
        listItem: ListItem,
        sourceCollection: String? = null,
        targetCollection: String,
        onSuccess: (() -> Unit)? = null
    ) {

        val listItemMap = listItem.toMap()

        // Legg til i targetCollection og fjern fra sourceCollection
        firestoreRepository.addToCollection(
            userID, listItemMap, targetCollection,
            onSuccess = {
                Log.d("FirestoreAdd", "Successfully added to $targetCollection for user $userID")

                if (sourceCollection != null) {
                    firestoreRepository.removeFromCollection(
                        userID, listItem, sourceCollection,
                        onSuccess = {
                            Log.d("FirestoreRemove", "Successfully removed from $sourceCollection")

                            updateUserCollections(listItem, sourceCollection, targetCollection)

                            onSuccess?.invoke()
                        },
                        onFailure = {
                            Log.e("FirestoreRemove", "Failed to remove from $sourceCollection")

                            updateUserCollections(listItem, sourceCollection, targetCollection)
                        },
                        onNotFound = {
                            Log.e("FirestoreRemove", "Item not found in $sourceCollection")

                            updateUserCollections(listItem, sourceCollection, targetCollection)
                        }
                    )
                } else {
                    updateUserCollections(listItem, sourceCollection, targetCollection)
                }
            },
            onFailure = { e ->
                Log.e("FirestoreAdd", "Failed to add to $targetCollection for user $userID", e)
            }
        )
    }

    fun updateScoreInCollection(collection: String, listItem: ListItem, score: Int) {

        val user = loggedInUser.value
        val userID = user?.id

        if (userID != null) {

            val listItemID = listItem.id

            listItem.score = score

            firestoreRepository.updateScoreField(userID, listItemID, score, collection,
                onSuccess = {
                    if(listItem.loggedInUsersFavorite) {
                        firestoreRepository.updateScoreField(
                            userID,
                            listItemID,
                            score,
                            "favoriteCollection",
                            onFailure = { Log.d("UserViewModel", "updateScoreInCollection failed for listItem $listItemID in favoriteCollection")})

                    }
                },
                onFailure = { /* */ })
            }
    }

    fun updateCurrentEpisodeInCollection(collection: String, listItem: ListItem, currentEpisode: Int) {

        val user = loggedInUser.value
        val userID = user?.id

        if (userID != null) {

            val listItemID = listItem.id

            firestoreRepository.updateCurrentEpisodeField(
                userID,
                listItemID,
                listItem.currentEpisode,
                collection,
                onSuccess = {
                    if(listItem.loggedInUsersFavorite) {
                        firestoreRepository.updateCurrentEpisodeField(
                            userID,
                            listItemID,
                            listItem.currentEpisode,
                            "favoriteCollection",
                            onFailure = { Log.d("UserViewModel", "updateCurrentEpisodeField failed for listItem $listItemID in $collection")})
                    }
                    listItem.currentEpisode = currentEpisode
                },
                onFailure = { /* Feilhåndtering */ }
            )
        }
    }

    fun removeProductionFromCollections(userID: String, listItem: ListItem, sourceCollection: String?) {


        // Konverter listItem til map for lagring i Firestore
        val listItemMap = listItem.toMap()

        // Legg til i targetCollection og fjern fra sourceCollection
        if (sourceCollection != null) {
            firestoreRepository.removeFromCollection(
                userID, listItem, sourceCollection,
                onSuccess = {
                    Log.d("FirestoreRemove", "Successfully removed from $sourceCollection")
                    updateUserCollections(listItem, sourceCollection)
                },
                onFailure = {
                    Log.e("FirestoreRemove", "Failed to remove from $sourceCollection")
                    updateUserCollections(listItem, sourceCollection)
                },
                onNotFound = {
                    Log.e("FirestoreRemove", "Item not found in $sourceCollection")
                    updateUserCollections(listItem, sourceCollection)
                }
            )
        }
    }

    private fun updateUserCollections(listItem: ListItem, sourceCollection: String? = null, targetCollection: String? = null) {
        val user = loggedInUser.value

        // Fjern fra sourceCollection og legg til i targetCollection
        when (sourceCollection) {
            "currentlyWatchingCollection" -> user?.currentlyWatchingCollection?.remove(listItem)
            "wantToWatchCollection" -> user?.wantToWatchCollection?.remove(listItem)
            "droppedCollection" -> user?.droppedCollection?.remove(listItem)
            "completedCollection" -> user?.completedCollection?.remove(listItem)
        }

        when (targetCollection) {
            "currentlyWatchingCollection" -> user?.currentlyWatchingCollection?.add(listItem)
            "wantToWatchCollection" -> user?.wantToWatchCollection?.add(listItem)
            "droppedCollection" -> user?.droppedCollection?.add(listItem)
            "completedCollection" -> user?.completedCollection?.add(listItem)
        }
    }

    fun updateProfileImage(imageUri : Uri) {

        viewModelScope.launch {

            try {
                val imageUrl = firestoreRepository.uploadProfileImage(imageUri) // Laster opp og får URL
                firestoreRepository.saveImageUrlToUserDoc(imageUrl) // Lagre URL i Firestore

                val updatedUser = loggedInUser.value?.copy(profileImageID = imageUrl)
                _loggedInUser.value = updatedUser

                Log.d("Upload", "Profilbilde oppdatert!")

            } catch (e: Exception) {
                Log.e("Upload", "Feil: ${e.message}")
            }

        }
    }


    fun publishReview(
        collectionID: String,
        productionID: String,
        reviewID: String,
        reviewData: Map<String, Any>,
        onSuccess: () -> Unit?) {

        viewModelScope.launch {

            val user = loggedInUser.value
            val userID = user?.id

            if (userID != null) {
                firestoreRepository.publishReview(collectionID, productionID, reviewID, reviewData,
                    onSuccess = {
                        viewModelScope.launch {
                            firestoreRepository.addReviewToUser(userID, reviewID)

                            user.myReviews.add(reviewID)
                            Log.d("tester", user.myReviews.toString())
                            onSuccess()
                        }
                    })
            }
        }

    }


    private fun convertUserJsonToUserObject(userJson: Map<String, Any>?): User? {

        if (userJson == null) return null

        val moshi = Moshi.Builder()
            .add(
                PolymorphicJsonAdapterFactory.of(Production::class.java, "type")
                    .withSubtype(Movie::class.java, "Movie")
                    .withSubtype(TVShow::class.java, "TVShow")
            )
            .add(FirebaseTimestampAdapter())  // Adapter for Firestore Timestamps
            .add(UUIDAdapter())  // Adapter for UUID
            .addLast(KotlinJsonAdapterFactory())  // For Kotlin-klasser
            .build()

        // Konverter Map til JSON-streng
        val jsonAdapter = moshi.adapter(User::class.java)
        val json = mapToJson(userJson, moshi)

        // Deserialiser JSON-streng til User-objekt
        return jsonToUser(json, jsonAdapter)
    }

    private fun mapToJson(map: Map<String, Any>, moshi: Moshi): String {
        return moshi.adapter(Map::class.java).toJson(map)
    }

    private fun jsonToUser(json: String, jsonAdapter: JsonAdapter<User>): User? {
        return jsonAdapter.fromJson(json)
    }




}


