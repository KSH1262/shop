// orderHist.html
function cancelOrder(orderId) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/order/" + orderId + "/cancel";

    $.ajax({
        url: url,
        type: "POST",
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(result, status) {
            alert("주문이 취소되었습니다.");
            // currentPage 변수 사용 (HTML에서 주입됨)
            location.href = '/orders/' + currentPage;
        },
        error: function(jqXHR, status, error) {
            if (jqXHR.status == 401) {
                alert('로그인 후 이용해주세요');
                location.href = '/members/login';
            } else {
                alert(jqXHR.responseText);
            }
        }
    });
}