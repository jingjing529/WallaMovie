let search = $("#search");
/*
 * CS 122B Project 4. Autocomplete Example.
 *
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 *
 * This example implements the basic features of the autocomplete search, features that are
 *   not implemented are mostly marked as "TODO" in the codebase as a suggestion of how to implement them.
 *
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 *
 */


/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here

    let cacheData = sessionStorage.getItem(query);
    if (cacheData !== null) {
        console.log("query in cache")
        let jsonData = JSON.parse(cacheData);
        doneCallback( { suggestions: jsonData } );
        console.log(jsonData)
        return;
    }
    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    console.log("sending AJAX request to backend Java Servlet")
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "hero-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here
    sessionStorage.setItem(query, JSON.stringify(jsonData))
    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    let url = window.location.href;
    window.location.href = url.replace("index.html","single-movie.html?id=") + suggestion["data"]["heroID"];
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["heroID"])
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3,
});




function handleGenreResult(resultData) {
    // console.log("handleMovieListResult: link movie-list.html to home");
    // let homeElement = jQuery("#home");
    // homeElement.append('<a href="movie-list.html">' + "Home" + '</a>');
    console.log("handleResult: check out page");
    let homeElement = jQuery("#home");
    homeElement.append('<li><a href="shopping-cart.html">' + "Check Out" + '</a></li>');
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');


    console.log("handleGenreResult: populating genre list from resultData");

    let GenreBodyElement = jQuery("#genre_body");
    for (let i = 0; i < resultData.length; i++) {
        GenreBodyElement.append('<a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:' + resultData[i]['genre'] + '">' + resultData[i]["genre"] + '</a>');
    }
    console.log("handleAlphaResult: populating alphabet list from resultData");

    let AlphaBodyElement = jQuery("#alpha_body");
    for (let i = 0; i <= 9; i++) {
        AlphaBodyElement.append('<a href="movie-list.html?num=10&page=1&sort=r0t1&input=alpha:' + i + '">' + i + '</a>');
    }
    for (let i = 65; i <= 90; i++) {
        AlphaBodyElement.append('<a href="movie-list.html?num=10&page=1&sort=r0t1&input=alpha:' + String.fromCharCode(i) + '">' + String.fromCharCode(i) + '</a>');
    }
    AlphaBodyElement.append('<a href="movie-list.html?num=10&page=1&sort=r0t1&input=alpha:' + '*' + '">' + '*' + '</a>');
}
function handleSearch(searchEvent) {
    console.log("submit search form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    searchEvent.preventDefault();

    $.ajax(
        "api/index", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: search.serialize(),
            success: handleSearchResult
        }
    );
}
function handleSearchResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    let newURL = "input=";
    if (resultDataJson["sort_title"]) {
        newURL += "title:" + resultDataJson["sort_title"] + ":";
    }
    if (resultDataJson["sort_year"]){
        newURL += "year:" + resultDataJson["sort_year"] + ":";
    }
    if (resultDataJson["sort_director"]){
        newURL += "director:" + resultDataJson["sort_director"] + ":";
    }
    if (resultDataJson["sort_name"]){
        newURL += "name:" + resultDataJson["sort_name"] + ":";
    }
    // if (newURL == "input="){
    //     newURL = "";
    // }
    // else {
    //     newURL -= "-";
    // }
    window.location.replace("movie-list.html?num=10&page=1&sort=r0t1&" + newURL);
}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/index", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});


search.submit(handleSearch);

