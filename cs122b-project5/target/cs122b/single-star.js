
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


    console.log("handleResult: populating star name from resultData");
    let starNameElement = jQuery("#star_name");
    starNameElement.append("<p>" + resultData[0]["star_name"] + "</p>");

    console.log("handleResult: populating star info from resultData");
    let starInfoElement = jQuery("#star_info");
    starInfoElement.append("<p>" + resultData[0]["star_dob"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    let movieTableBodyElement = jQuery("#movie_table_body");
    for (let i = 0; i < resultData.length -1; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}

let starId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});