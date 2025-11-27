// memberForm.html

$(document).ready(function () {
    var errorMessage = [[${errorMessage}]];
    if (errorMessage != null) {
        alert(errorMessage);
    }

    // 주소 검색 버튼(JS 통일)
    $("#postcodify_search_button").postcodifyPopUp();
});
