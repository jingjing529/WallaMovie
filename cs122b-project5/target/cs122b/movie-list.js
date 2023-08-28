let cart_form = $("#cart_form");
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


function updateUrlWithPageNumber(page) {
    let url = window.location.href;
    let newUrl = url.replace(/(page=)(\d+)/, `$1${page}`);
    return newUrl;
}

function handleMovieListResult(resultData) {
    if (resultData.length === 0 && parseInt(page) > 1) {
        // page = parseInt(page) -1;
        // let newUrl = updateUrlWithPageNumber(page);
        // history.pushState({}, '', newUrl);
        // fetchMovieList(num, page, sort, input);
        window.location.href = sessionStorage.getItem("previousURL");
        return;
    }

    // console.log("handleMovieListResult: link movie-list.html to home");
    // let homeElement = jQuery("#home");
    // homeElement.append('<a href="movie-list.html">' + "Home" + '</a>');
    console.log("handleResult: prev, next and search page");
    let homeElement = jQuery("#home");
    homeElement.append('<li><a href="index.html">' + "Home" + '</a></li>');
    homeElement.append('<li><a href="shopping-cart.html">' + "Check Out" + '</a></li>');
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');

    console.log("handleMovieListResult: populating movie list table from resultData");
    let num = jQuery("#num");
    num.append("<li><a href=\"#\" id=\"num_10\" onclick=\"updateNum(10)\">10</a></li>\n" +
        "                    <li><a href=\"#\" id=\"num_25\" onclick=\"updateNum(25)\">25</a></li>\n" +
        "                    <li><a href=\"#\" id=\"num_50\" onclick=\"updateNum(50)\">50</a></li>\n" +
        "                    <li><a href=\"#\" id=\"num_100\" onclick=\"updateNum(100)\">100</a></li>");

    let sort_nav = jQuery("#sort_nav");
    sort_nav.append("<li><a href=\"#\" id=\"sort_t1r0\" onclick=\"updateSort('t1r0')\">Title ↑ Rating ↓</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_t0r0\" onclick=\"updateSort('t0r0')\">Title ↓ Rating ↑</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_t1r1\" onclick=\"updateSort('t1r1')\">Title ↑ Rating ↑</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_t0r1\" onclick=\"updateSort('t0r1')\">Title ↓ Rating ↓</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_r1t0\" onclick=\"updateSort('r1t0')\">Rating ↑ Title ↓</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_r0t1\" onclick=\"updateSort('r0t1')\">Rating ↓ Title ↑</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_r1t1\" onclick=\"updateSort('r1t1')\">Rating ↑ Title ↑</a></li>\n" +
        "                    <li><a href=\"#\" id=\"sort_r0t0\" onclick=\"updateSort('r0t0')\">Rating ↓ Title ↓</a></li>");

    let prevNext = jQuery("#prevNext");
    prevNext.append("<li><a href = \"#\" onclick=\"updatePage(1)\">Next</a></li>\n" +
        "            <li><a href = \"#\" onclick=\"updatePage(-1)\">Prev</a></li>");

    let movieListTableBodyElement = jQuery("#movieList_table_body");
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['id'] + '">'
            + resultData[i]["title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";

        rowHTML += "<th>" + '<a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:' + resultData[i]['genre1'] + '">' + resultData[i]["genre1"] + '</a>'  +
            '<p></p>'+
            '<a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:' + resultData[i]['genre2'] + '">' + resultData[i]["genre2"] + '</a>' ;
        rowHTML += '<p></p>';
        rowHTML += '<a href="movie-list.html?num=10&page=1&sort=r0t1&input=genre:' + resultData[i]['genre3'] + '">' + resultData[i]["genre3"] + '</a>' ;
        rowHTML += "</th>";

        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + resultData[i]['starId1'] + '">'
            + resultData[i]["star1"] + '</a>';
        // console.log(resultData[i]["starId2"]);
        if (resultData[i]["starId2"] != null){
            rowHTML += '<p></p>'+
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-star.html?id=' + resultData[i]['starId2'] + '">'
            + resultData[i]["star2"] + '</a>';}
        if (resultData[i]['starId3'] != null){
            rowHTML += '<p></p>';
            rowHTML += '<a href="single-star.html?id=' + resultData[i]['starId3'] + '">'
                + resultData[i]["star3"] +    // display star_name for the link text
                '</a>';
        }
            // Add a link to single-star.html with id passed with GET url parameter
        rowHTML += "</th>";
        if (resultData[i]['rating'] != null){
            rowHTML += "<th>" + resultData[i]["rating"] + "</th>";}
        else{
            rowHTML += "<th>N/A</th>";
        }
        rowHTML += "<th>";
        rowHTML += "<button type = 'button' onclick = \"addFunction(this.value)\" name = 'button' value =" + [encodeURIComponent(resultData[i]["title"]), resultData[i]["id"]] + ">Add</button>";
        rowHTML +="</th>";
        rowHTML += "</tr>";
        movieListTableBodyElement.append(rowHTML);
    }
    sessionStorage.setItem("previousURL", window.location.href);
}
function fetchMovieList(num, page, sort, input) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movie-list?num=" + num + "&page=" + page + "&sort=" + sort + "&input=" + input,
        success: (resultData) => handleMovieListResult(resultData)
    });
}

let num = getParameterByName('num');
let page = getParameterByName('page')
let sort = getParameterByName('sort');
let input = getParameterByName('input');
let title = getParameterByName("movie-title");
console.log("title");
console.log(title);

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

function updateQueryStringParameter(uri, key, value) {
    const re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    const separator = uri.indexOf("?") !== -1 ? "&" : "?";
    if (uri.match(re)) {
        return uri.replace(re, "$1" + key + "=" + value + "$2");
    } else {
        return uri + separator + key + "=" + value;
    }
}
function addQueryStringParameter(uri, key, value) {
    const re = new RegExp("([?&])" + key + "=.*?(&|$)", "i");
    const separator = uri.indexOf("?") !== -1 ? "&" : "?";
    if (uri.match(re)) {
        const currentValue = parseInt(uri.match(re)[0].split("=")[1], 10);
        let newValue = Math.max(currentValue + value,1);
        return uri.replace(re, "$1" + key + "=" + newValue + "$2");
    } else {
        return uri + separator + key + "=" + value;
    }
}

function updateSort(sortValue) {
    let currentUrl = window.location.href;
    currentUrl = updateQueryStringParameter(currentUrl, "sort", sortValue);
    window.location.href = currentUrl;
    // The page will reload, so there is no need to call any function to update the movie list
}

function updateNum(numValue) {
    let currentUrl = window.location.href;
    currentUrl = updateQueryStringParameter(currentUrl, "num", numValue);
    window.location.href = currentUrl;
    // The page will reload, so there is no need to call any function to update the number of movies per page
}

function updatePage(pageValue) {
    let currentUrl = window.location.href;
    currentUrl = addQueryStringParameter(currentUrl, "page", pageValue);
    window.location.href = currentUrl;
    // The page will reload, so there is no need to call any function to update
}

fetchMovieList(num, page, sort, input);
