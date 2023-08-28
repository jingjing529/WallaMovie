let payment_form = $("#payment_form");


function handleResult(resultData){

    let single_movie_add_element = jQuery("#pass_value");
    let allItems = JSON.parse(sessionStorage.getItem("previousItem"));
    single_movie_add_element.append("<p>" + 'Look at you, you just spent $' + allItems.length * 10 + ' on movies :)'+ "</p>");
    single_movie_add_element.append( "<input type='hidden' id='movie' name='movie' value=" + allItems + "/>");
}
/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle payment response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);
    if (resultDataJson["status"] === "success") {
        window.location.replace("confirmation.html"); //for testing
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#payment_error_message").text(resultDataJson["message"]);
    }
}


/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitPaymentForm(formSubmitEvent) {
    console.log("submit Payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    //

    $.ajax(
        "api/payment", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}


jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/payment", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the MovieListServlet
});


// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);

