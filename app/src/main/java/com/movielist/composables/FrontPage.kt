package com.movielist.composables

import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movielist.R
import com.movielist.data.ListItem
import com.movielist.data.Review
import com.movielist.data.Show
import com.movielist.data.User
import com.movielist.ui.theme.Gray
import com.movielist.ui.theme.White
import com.movielist.ui.theme.*
import java.util.Calendar
import kotlin.random.Random
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.movielist.MyApi
import com.movielist.data.Movie
import com.movielist.data.MovieResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


//private val BASE_URL = "https://jsonplaceholder.typicode.com/" -> test api
private val BASE_URL ="https://moviesdatabase.p.rapidapi.com/"
private val API_KEY = "09f23523ebmshad9f7b2ebe7b44bp1ecd5bjsn35bb315b63a3" // api key, må være med! Har med autentisering å gjøre
private val API_HOST = "moviesdatabase.p.rapidapi.com" // host link, må være med! Har med autentisering å gjøre, lik for alle
private val TAG: String = "CHECK_RESPONSE" // Skriv inn i LogCat for å se output fra api

// Autentiserer API nøkkelen. Gis i rapidAPI sin code snippet (tror man måtte opprette bruker for å få den)
private val apiKeyInterceptor = Interceptor { chain ->
    val original = chain.request()
    val request = original.newBuilder()
        .addHeader("x-rapidapi-key", API_KEY)
        .addHeader("x-rapidapi-host", API_HOST)
        .build()
    chain.proceed(request)
}

// Oppretter en OkHttpClient med apiKeyInterceptor
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(apiKeyInterceptor)
    .build()

// Oppretter en Retrofit instans for å gjøre et API call
private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Henter filmer (bare titler for øyeblikket)
private fun getAllShows() {
    val api = retrofit.create(MyApi::class.java)

    api.getTitles().enqueue(object : Callback<MovieResponse> {

        override fun onResponse(
            call: Call<MovieResponse>,
            response: Response<MovieResponse>
        ) {
            if (response.isSuccessful) {
                response.body()?.let { movieResponse ->
                    val movieList = movieResponse.results
                    for (movie in movieList) {
                        Log.i(TAG, "Movie Title: ${movie.titleText.text}")
                    }
                }
            } else {
                Log.i(TAG, "onResponse: Failed with response code ${response.code()}")
            }
        }
        override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
            Log.i(TAG, "onFailure: ${t.message}")
        }
    })
}

@Composable
fun FrontPage () {

    // Metoden som henter filmer/shows fra APIet
    getAllShows()


    //Temporary code: DELETE THIS CODE
    /*
    val listItemList = mutableListOf<ListItem>()
    for (i in 0..12) {
        listItemList.add(
            ListItem(
                currentEpisode = i,
                score = Random.nextInt(0, 10),
                show =  Show(
                    title = "Silo",
                    length = 12,
                    imageID = R.drawable.silo,
                    imageDescription = "Silo TV Show",
                    releaseDate = Calendar.getInstance()
                )
            )
        )
    }

    val showList = mutableListOf<Show>()

    for (i in 0..12) {
        showList.add(
            Show(
            title = "Silo",
            length = 12,
            imageID = R.drawable.silo,
            imageDescription = "Silo TV Show",
            releaseDate = Calendar.getInstance()
            )
        )
    }

    val reviewList = mutableListOf<Review>()
    val user = User(
        id = "testid",
        userName = "User Userson",
        email = "test@email.no",
        friendList = emptyList(),
        myReviews = emptyList(),
        favoriteCollection = emptyList(),
        profileImageID = R.drawable.profilepicture,
        completedShows = listItemList,
        wantToWatchShows = listItemList,
        droppedShows = listItemList,
        currentlyWatchingShows = listItemList
    )
    for (i in 0..6) {
        reviewList.add(
            Review(
                score = Random.nextInt(0, 10), //<- TEMP CODE: PUT IN REAL CODE
                reviewer = user,
                show = listItemList[1].show,
                reviewBody = "It’s reasonably well-made, and visually compelling," +
                        "but it’s ultimately too derivative, and obvious in its thematic execution," +
                        "to recommend..",
                postDate = Calendar.getInstance(),
                likes = Random.nextInt(0, 100) //<- TEMP CODE: PUT IN REAL CODE
            )
        )
    }
    */
    //^^^KODEN OVENFOR ER MIDLERTIDIG. SLETT DEN.^^^^

    //Front page graphics
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        //Front page content
        item {
            //CurrentlyWatchingScroller(listItemList)
        }

        item {
           // PopularShowsAndMovies(showList)
        }

        item {
           // YourFriendsJustWatched(listItemList)
        }

        item {
            //Top reviews this week:
            //ReviewsSection(
             //   reviewList = reviewList,
                //header = "Top reviews this week"
           // )
        }

        item {
            /*Adds empty space the size of the bottom nav bar to ensure content don't dissapear
            behind it*/
            Spacer(modifier = Modifier.height(bottomNavBarHeight))
        }

    }

}

@Composable
fun CurrentlyWatchingScroller (
    listOfShows: List<ListItem>
    //listOfShows: List<ListItem>
) {

    LazyRow (
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(start = horizontalPadding, end = 0.dp)
    ) {
        items (listOfShows.size) {i ->
            CurrentlyWatchingCard(
                imageId = listOfShows[i].show.imageID,
                imageDescription = listOfShows[i].show.imageDescription,
                title = listOfShows[i].show.title,
                showLenght = listOfShows[i].show.length,
                episodesWatched = listOfShows[i].currentEpisode)
        }
    }
}

@Composable
fun CurrentlyWatchingCard (
    imageId: Int = R.drawable.noimage,
    imageDescription: String = "Image not available",
    title: String,
    showLenght: Int,
    episodesWatched: Int,
    modifier: Modifier = Modifier

    ) {

    var watchedEpisodesCount: Int by remember {
        mutableIntStateOf(episodesWatched)
    }

    var buttonText by remember {
        mutableStateOf(generateButtonText(episodesWatched, showLenght))
    }

    //Card container
    Card (
        modifier = modifier
            .width(350.dp),
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Gray)

    ){
        //card content
        Column(modifier = Modifier
            .height(265.dp+ topPhoneIconsBackgroundHeight)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = (topPhoneIconsBackgroundHeight+10.dp),
                bottom = 10.dp))
        {
            //Main image
            Image(
                painter = painterResource(id = imageId),
                contentDescription = imageDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp))

            //Content under image
            Column(modifier = Modifier
                .fillMaxSize()
                )
            {
                //Title and episodes watched
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    //Title
                    Text(
                        title,
                        style = TextStyle(
                            color = White,
                            fontSize = 18.sp,
                            fontWeight = weightRegular
                            )
                    )
                    //Episodes watched
                    Text (
                        "Ep $watchedEpisodesCount of $showLenght",
                        style = TextStyle(
                            color = White,
                            fontSize = 18.sp,
                            fontWeight = weightLight
                        )
                    )
                }

                //Progress bar
                ProgressBar(currentNumber = watchedEpisodesCount, endNumber = showLenght)

                //Mark as watched button
                Button(
                    onClick = {
                        //Button onclick function
                        if ( watchedEpisodesCount < showLenght) {
                            watchedEpisodesCount++
                        }

                        buttonText = generateButtonText(watchedEpisodesCount, showLenght)
                    },
                    shape = RoundedCornerShape(5.dp),
                    colors = ButtonDefaults.buttonColors(Purple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(vertical = 5.dp)
                ) {
                    //Button text
                    Text(
                        buttonText,
                        fontSize = headerSize,
                        fontWeight = weightRegular,
                        color = DarkGray
                    )
                }
            }

        }
    }
}

@Composable
fun PopularShowsAndMovies (
    listOfShows: List<Show>
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = verticalPadding)
    ) {
        //Header
        Text(
            "Popular shows and movies",
            fontFamily = fontFamily,
            fontSize = headerSize,
            fontWeight = weightBold,
            color = White,
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = horizontalPadding)
        )
        LazyRow (
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(start = horizontalPadding, end = 0.dp)
        ){
            items (listOfShows.size) {i ->
                ShowImage(
                    imageID = listOfShows[i].imageID,
                    imageDescription = listOfShows[i].imageDescription
                    )
            }
        }

    }
}

@Composable
fun YourFriendsJustWatched (
    listOfShows: List<ListItem>
) {
    //Container collumn
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = verticalPadding)
    ) {
        //Header
        Text(
            "Your friends just watched",
            fontFamily = fontFamily,
            fontSize = headerSize,
            fontWeight = weightBold,
            color = White,
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = horizontalPadding)
        )
        //Content
        LazyRow (
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(start = horizontalPadding, end = 0.dp)
        ){
            items (listOfShows.size) {i ->
                //Info for each show
                Column (
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    ShowImage(
                        imageID = listOfShows[i].show.imageID,
                        imageDescription = listOfShows[i].show.imageDescription
                    )
                    //Friend Info
                    FriendsWatchedInfo(
                        profileImageID = R.drawable.profilepicture,
                        profileName = "User Userson", //TEMP DELETE THIS
                        episodesWatched = i,
                        showLenght = listOfShows[i].show.length,
                        score = listOfShows[i].score
                    )
                }


            }
        }

    }
}

@Composable
fun FriendsWatchedInfo(
    profileImageID: Int,
    profileName: String,
    episodesWatched: Int,
    showLenght: Int,
    score: Int = 0
) {
    Row(
        horizontalArrangement =  Arrangement.spacedBy(3.dp)
    ) {
        ProfileImage(
            imageID = profileImageID,
            userName = profileName
        )
        //Episode Count and Score
        Column (
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ){
            Text(
                text = "Ep $episodesWatched of $showLenght",
                color = White,
                fontFamily = fontFamily,
                fontWeight = weightLight,
                fontSize = 12.sp
            )
            ScoreGraphics(
                score = score
            )
        }
    }

}

//Utility Functions
fun generateButtonText(
    episodesWatched: Int,
    showLenght: Int)
: String
{
    if (episodesWatched+1 == showLenght) {
        return "Mark as completed"
    } else if ( episodesWatched == showLenght){
        return "Add a rating"
    }
    else {
        return "Mark episode ${episodesWatched + 1} as watched"
    }

}