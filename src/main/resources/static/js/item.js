// itemDtl.html
$(document).ready(function(){

    calculateTotalPrice();

    $("#count").change( function(){
        calculateTotalPrice();
    });
});

function calculateTotalPrice(){
    var count = $("#count").val();
    var price = $("#price").val();
    var totalPrice = price*count;
    $("#totalPrice").html(totalPrice + '원');
}

function order(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/order";
    var paramData = {
        itemId : $("#itemId").val(),
        count : $("#count").val()
    };

    var param = JSON.stringify(paramData);

    $.ajax({
        url      : url,
        type     : "POST",
        contentType : "application/json",
        data     : param,
        beforeSend : function(xhr){
            /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache   : false,
        success  : function(result, status){
            alert("주문이 완료 되었습니다.");
            location.href='/';
        },
        error : function(jqXHR, status, error){

            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요');
                location.href='/members/login';
            } else{
                alert(jqXHR.responseText);
            }

        }
    });
}

function addCart(){
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    var url = "/cart";
    var paramData = {
        itemId : $("#itemId").val(),
        count : $("#count").val()
    };

    var param = JSON.stringify(paramData);

    $.ajax({
        url      : url,
        type     : "POST",
        contentType : "application/json",
        data     : param,
        beforeSend : function(xhr){
            /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
            xhr.setRequestHeader(header, token);
        },
        dataType : "json",
        cache   : false,
        success  : function(result, status){
            alert("상품을 장바구니에 담았습니다.");
            location.href='/';
        },
        error : function(jqXHR, status, error){

            if(jqXHR.status == '401'){
                alert('로그인 후 이용해주세요');
                location.href='/members/login';
            } else{
                alert(jqXHR.responseText);
            }

        }
    });
}

$(document).ready(function() {
// 여기를 수정했습니다: id="reviewItemId"로 변경
var itemId = $('#reviewItemId').val();
loadReviews(itemId);

$('#submitReviewBtn').off('click').on('click', function(event) {
    event.preventDefault();

    var $submitBtn = $(this);
        $submitBtn.prop('disabled', true);

    var rating = $('#rating').val();
    var comment = $('#comment').val();
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    if (!comment.trim()) {
        $('#reviewError').text('리뷰 내용을 입력해주세요.');
        return;
    } else {
        $('#reviewError').text('');
    }

    $.ajax({
        url: '/review/' + itemId,
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ rating: rating, comment: comment }),
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function(result) {
            alert(result);
            $('#comment').val(''); // 입력 필드 초기화
            loadReviews(itemId); // 리뷰 목록 새로고침
        },
        error: function(jqXHR, status, error) {
            if (jqXHR.status == '400') { // Bad Request (유효성 검사 실패)
                alert(jqXHR.responseText);
            } else if (jqXHR.status == '409') { // Conflict (중복 리뷰)
                alert(jqXHR.responseText);
            } else {
                alert('리뷰 등록에 실패했습니다: ' + jqXHR.responseText);
            }
        },
        complete: function() { // 요청이 완료되었을 때 (성공이든 실패든)
            $submitBtn.prop('disabled', false); // 버튼 다시 활성화
        }

    });
});

// 리뷰 로드 함수
function loadReviews(itemId) {
    $.ajax({
        url: '/item/' + itemId + '/reviews',
        type: 'GET',
        success: function(reviews) {
            var reviewListHtml = '';
            if (reviews.length === 0) {
                $('#noReviewsMessage').show();
            } else {
                $('#noReviewsMessage').hide();
                reviews.forEach(function(review) {
                    var stars = '';
                    for (var i = 0; i < review.rating; i++) {
                        stars += '★';
                    }
                    for (var i = review.rating; i < 5; i++) {
                        stars += '☆';
                    }

                    reviewListHtml += `
                        <div class="card mb-3">
                            <div class="card-body">
                                <h5 class="card-title">${stars} <small class="text-muted">${review.memberName}</small></h5>
                                <p class="card-text">${review.comment}</p>
                                <p class="card-text"><small class="text-muted">${new Date(review.regTime).toLocaleString()}</small></p>
                                <div> <button type="button" class="btn btn-danger btn-sm delete-review-btn" data-review-id="${review.id}"
                                            data-review-member-email="${review.memberEmail}"
                                            data-current-user-email="${currentLoggedInUserEmail}"
                                            style="display: none;">삭제</button>
                                </div>
                            </div>
                        </div>
                    `;
                });
            }
            $('#reviewList').html(reviewListHtml);
            updateDeleteButtonsVisibility(); // 삭제 버튼 가시성 업데이트
        },
        error: function(jqXHR, status, error) {
            console.error('리뷰 로드 실패:', error);
            $('#reviewList').html('<p class="text-danger text-center">리뷰를 불러오는 데 실패했습니다.</p>');
        }
    });
}

// 삭제 버튼 클릭 이벤트
$(document).off('click', '.delete-review-btn').on('click', '.delete-review-btn', function(event) {
    event.preventDefault();
    if (confirm('정말로 이 리뷰를 삭제하시겠습니까?')) {
        var reviewId = $(this).data('review-id');
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            url: '/review/' + reviewId,
            type: 'DELETE',
            beforeSend: function(xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function(result) {
                alert(result);
                loadReviews(itemId); // 리뷰 목록 새로고침
            },
            error: function(jqXHR, status, error) {
                alert('리뷰 삭제에 실패했습니다: ' + jqXHR.responseText);
            }
        });
    }
});

// 삭제 버튼 가시성 업데이트 함수
function updateDeleteButtonsVisibility() {

    $('.delete-review-btn').each(function() {
        var reviewMemberEmail = $(this).data('review-member-email');

        // 리뷰 작성자의 이메일과 현재 로그인 사용자의 이메일이 같으면 삭제 버튼 표시
        // currentLoggedInUserEmail 변수를 직접 사용합니다.
        if (reviewMemberEmail && currentLoggedInUserEmail && currentLoggedInUserEmail === reviewMemberEmail) {
            $(this).show();
        } else {
            $(this).hide();
        }
    });
}
});





// itemForm.html
$(document).ready(function(){
    var errorMessage = $("#errorContainer").data("error-message");
    if (errorMessage) {
        alert(errorMessage);
    }
    bindDomEvent();
});

function bindDomEvent(){
    $(".custom-file-input").on("change", function(){
        var fileName = $(this).val().split("\\").pop();
        var fileExt = fileName.substring(fileName.lastIndexOf(".")+1);
        fileExt = fileExt.toLowerCase();

        if(fileExt != "jpg" && fileExt != "jpeg" && fileExt != "gif" && fileExt != "png" && fileExt != "bmp"){
          alert("이미지 파일만 등록이 가능합니다.");
          return;
        }
        $(this).siblings(".custom-file-label").html(fileName);
    });
}

// itemMng.html
$(document).ready(function(){
    $("#searchBtn").on("click",function(e) {
        e.preventDefault();
        page(0);
    });
});

function page(page){
    var searchDateType = $("#searchDateType").val();
    var searchSellStatus = $("#searchSellStatus").val();
    var searchBy = $("#searchBy").val();
    var searchQuery = $("#searchQuery").val();

    location.href="/admin/items/" + page + "?searchDateType=" + searchDateType
    + "&searchSellStatus=" + searchSellStatus
    + "&searchBy=" + searchBy
    + "&searchQuery=" + searchQuery;
}
