
function handleResult(resultData) {

    let homeElement = jQuery("#home");
    let url = resultData[resultData.length-1]["movie_page"];
    console.log("getting movie page url from result data");
    console.log(url);
    homeElement.append('<li><a href="index.html">' + "Home" + '</a></li>');
    if (url){
        homeElement.append('<li><a href=movie-list.html?' + url + '>' + "Movie List" + '</a></li>');
    }
    homeElement.append('<li><a href="login.html">' + "Log Out" + '</a></li>');

    let proceed = jQuery("#proceed");
    proceed.append("<li><a href=\"payment.html\"> Proceed to Payment--> </a></li>");



    console.log("shopping-cart.js");
    console.log("handleResult: link movie-list.html to home");
    let allItems = JSON.parse(sessionStorage.getItem("previousItem"));
    allItems.sort();
    console.log(allItems);
    console.log(allItems.length);

    console.log("handleResult: populating star table from resultData");
    let cartTableBodyElement = jQuery("#cart_table_body");
    let total = 0;
    let count = 1;
    let i = 0;
    while (i < allItems.length - 1){
        while (allItems[i] === allItems[i+1]){
                count ++;
                i ++;
        }
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" + "<button type = 'button' onclick = \"deleteFunction(this.value)\" name = 'button' value =" + allItems[i] + ">x</button>"
        + "</th>";
        rowHTML +=
            "<th>" + decodeURIComponent(allItems[i].split(",")[0]) + "</th>"; //this should be getting the id and title from session.
        rowHTML += "<th>" + "$10" + "</th>";
        rowHTML += "<th>" + "<button type = 'button' onclick = \"AddFunction(this.value)\" name = 'button' value =" + allItems[i] + ">+</button>" + "</th>";
        rowHTML += "<th>" + count + "</th>";
        rowHTML += "<th>" + "<button type = 'button' onclick = \"MinusFunction(this.value)\" name = 'button' value =" + allItems[i] + ">-</button>" + "</th>";
        total += count * 10;
        rowHTML += "<th>" + count * 10 + "</th>";
        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
        i++;
        count = 1;
    }

    if (allItems[allItems.length-1] !== allItems[allItems.length-2]){
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" + "<button type = 'button' onclick = \"deleteFunction(this.value)\" name = 'button' value =" + allItems[allItems.length-1] + "> x </button>"
        + "</th>";
        rowHTML +=
            "<th>" + decodeURIComponent(allItems[allItems.length-1].split(",")[0])+ "</th>"; //this should be getting the id and title from session.
        rowHTML += "<th>" + "$10" + "</th>";
        rowHTML += "<th>" + "<button type = 'button' onclick = \"AddFunction(this.value)\" name = 'button' value =" + allItems[allItems.length-1]+ "> + </button>" + "</th>";
        rowHTML += "<th>" + count + "</th>";
        rowHTML += "<th>" + "<button type = 'button' onclick = \"MinusFunction(this.value)\" name = 'button' value =" + allItems[allItems.length-1] + "> - </button>" + "</th>";
        total += count * 10;
        rowHTML += "<th>" + count * 10 + "</th>";
        rowHTML += "</tr>";
        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
    }
    let Total = jQuery("#total_price");
    Total.append('<p>'+ "Total is $" + total + '</p>');
}

// Makes the HTTP GET request and registers on success callback function handleResult

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/shopping-cart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

function deleteFunction(ItemDelete) {
    let previous = JSON.parse(sessionStorage.getItem("previousItem"));
    let new_previous = previous.filter(e => e !== ItemDelete);
    console.log(new_previous)
    sessionStorage.setItem("previousItem", JSON.stringify(new_previous));
    console.log('Deleted new item ' + ItemDelete);
    alert('Successfully deleted movie ' + decodeURIComponent(ItemDelete.split(",")[0]) + ' from your shopping cart!');
    window.location.reload();
}
function AddFunction(ItemAdd){
    let previous = JSON.parse(sessionStorage.getItem("previousItem"));
    previous.push(ItemAdd);
    sessionStorage.setItem("previousItem", JSON.stringify(previous));
    console.log('Increased Number of ' + ItemAdd);
    window.location.reload();
}

function MinusFunction(ItemAdd){
    let previous = JSON.parse(sessionStorage.getItem("previousItem"));
    for (let i = 0; i < previous.length; i ++){
        if (previous[i] === ItemAdd){
            previous.splice(i, 1);
            console.log('Deleted new item ' + ItemAdd);
            break;
        }
        // alert('Movie ' + decodeURIComponent(ItemAdd.split(",")[0])+ ' has been removed from your cart.');
            }

    sessionStorage.setItem("previousItem", JSON.stringify(previous));
    console.log('Decreased Number of ' + ItemAdd);
    window.location.reload();
}