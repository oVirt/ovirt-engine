$(function () {
    // Add login form submit handler that appends URL fragment to the form's
    // action URL. This way, web applications using URL fragment for their UI
    // navigation keep working as expected after the post-login redirect.
    $('#loginForm').submit(function (event) {
        var loginForm = $(this);
        var fragment = window.location.hash;
        var actionUrl = loginForm.attr('action');
        // Append fragment to form action.
        loginForm.attr('action', actionUrl + fragment);
    });
});