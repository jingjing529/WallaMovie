let single_movie_add = $("#single_movie_add");
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {
    console.log("handleResult: link movie-list.html to home");
    let homeElement = jQuery("#home");
    let count = resultData.length-1;
    let url = resultData[count]["movie_page"];
    console.log("getting movie page url from result data");
    console.log(url);
    homeElement.append('<li><a href="index.html">' + "Home" + '</a></li>');
    homeElement.append('<li><a href=movie-list.html?' + url + '>' + "Movie List" + '</a></li>');
    homeElement.append('<li><a href="shopping-cart.html">' + "Check Out" + '</a></li>');
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');

    let button = jQuery("#button");
    button.append("<li><button type = 'button' onclick = \"addFunction(this.value)\" name = 'button' value =" + [encodeURIComponent(resultData[0]["movie_title"]), resultData[0]["movie_id"]] + ">Add</button></li>");


    console.log("handleResult: populating movie title from resultData");
    let movieTitleElement = jQuery("#movie_title");
    movieTitleElement.append("<p>" + resultData[0]["movie_title"] + "</p>");

    console.log("handleResult: populating movie info from resultData");
    let movieInfoElement = jQuery("#movie_info");

    movieInfoElement.append(
        "<p>" + resultData[0]["movie_year"] + " / " + resultData[0]["movie_director"] + " / " + resultData[0]["rating"] + "</p>"
        + "<p>" + resultData[0]["genre"] + "</p>"
    );
    // console.log("handleResult: populating movie info from resultData");
    // let single_movie_add_element = jQuery("#single_movie_add");
    // single_movie_add_element.append("<button type = 'button' onclick = \"addFunction(this.value)\" name = 'button' value =" + [encodeURIComponent(resultData[0]["movie_title"]), resultData[0]["movie_id"]] + ">Add</button>");

    // single_movie_add_element.append( "<input type='hidden' id='movie-title' name='movie-title' value=" + encodeURIComponent(resultData[0]["movie_title"]) + "/>");
    // single_movie_add_element.append( "<input type='hidden' id='movie-id' name='movie-id' value=" + resultData[0]["movie_id"] + "/>");
    // console.log(resultData[0]);


    console.log("handleResult: populating star table from resultData");
    let movieTableBodyElement = jQuery("#star_table_body");
    for (let i = 0; i < Math.min(10, count); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
            + resultData[i]["star_name"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["star_dob"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});


function addFunction(ItemAdd) {
    alert('Successfully added movie ' + decodeURIComponent(ItemAdd.split(",")[0]) + ' into your shopping cart!');

    let array = JSON.parse(sessionStorage.getItem("previousItem"));
    if (!array){
        array = [];
    }
    array.push(ItemAdd);
    sessionStorage.setItem("previousItem", JSON.stringify(array));
    // previousItems.add(ItemAdd);
    console.log('Added new item ' + ItemAdd);
}
