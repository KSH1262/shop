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
            $('#reviewList').html('<p class="text-danger text-center">리뷰를 불러오는 데 실패했습니다.</p>');
        }
    });
}

function updateDeleteButtonsVisibility() {
    $('.delete-review-btn').each(function() {
        var reviewMemberEmail = $(this).data('review-member-email');
        if (reviewMemberEmail && typeof currentLoggedInUserEmail !== 'undefined' && currentLoggedInUserEmail === reviewMemberEmail) {
            $(this).show();
        } else {
            $(this).hide();
        }
    });
}

$(document).ready(function() {
    if ($('#reviewItemId').length) {
        var itemIdForReviews = $('#reviewItemId').val(); // itemIdForReviews 변수를 사용합니다.

        if (itemIdForReviews && !isNaN(itemIdForReviews)) {
            loadReviews(itemIdForReviews); // loadReviews 호출 시 itemIdForReviews 전달
        } else {
            // 상품 상세 페이지이지만 itemId가 없는 경우 (예외 상황 또는 URL 조작 등)
            $('#reviewList').html('<p class="text-danger text-center">리뷰를 불러올 상품 정보가 올바르지 않습니다.</p>');
            $('#reviewForm').hide(); // 리뷰 입력 폼 숨기기 (상품 ID가 유효하지 않을 경우)
        }

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
                $submitBtn.prop('disabled', false); // 에러 시 버튼 다시 활성화
                return;
            } else {
                $('#reviewError').text('');
            }

            // itemIdForReviews가 유효한지 다시 한번 확인
            if (!itemIdForReviews || isNaN(itemIdForReviews)) {
                 alert('유효한 상품 ID가 없어 리뷰를 등록할 수 없습니다.');
                 $submitBtn.prop('disabled', false);
                 return;
            }

            $.ajax({
                url: '/review/' + itemIdForReviews, // ★★★ itemIdForReviews 사용
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ rating: rating, comment: comment }),
                beforeSend: function(xhr) {
                    xhr.setRequestHeader(header, token);
                },
                success: function(result) {
                    alert(result);
                    $('#comment').val(''); // 입력 필드 초기화
                    loadReviews(itemIdForReviews); // ★★★ itemIdForReviews 사용
                },
                error: function(jqXHR, status, error) {
                    if (jqXHR.status == '400') {
                        alert(jqXHR.responseText);
                    } else if (jqXHR.status == '409') {
                        alert(jqXHR.responseText);
                    } else {
                        alert('리뷰 등록에 실패했습니다: ' + jqXHR.responseText);
                    }
                },
                complete: function() {
                    $submitBtn.prop('disabled', false); // 버튼 다시 활성화
                }
            });
        });

        // 리뷰 삭제 버튼 클릭 이벤트
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
                        loadReviews(itemIdForReviews); // ★★★ itemIdForReviews 사용
                    },
                    error: function(jqXHR, status, error) {
                        alert('리뷰 삭제에 실패했습니다: ' + jqXHR.responseText);
                    }
                });
            }
        });
    }
});




// itemForm.html
$(document).ready(function(){
    var errorMessageElement = $("#errorContainer");
    if (errorMessageElement.length) {
        var errorMessage = errorMessageElement.data("error-message");
        if (errorMessage) {
            alert(errorMessage);
        }
    }

    console.log("폼 제출 이벤트 감지 등록");
    bindDomEvent();

    if ($('#itemIdHidden').length) {
        var itemIdForForm = $('#itemIdHidden').val();

        // 1. 삭제 버튼 클릭 이벤트
        $('#deleteItemBtn').off('click').on('click', function(event) {
            event.preventDefault();

            if (!itemIdForForm) {
                alert('삭제할 상품 ID가 없습니다.');
                return;
            }

            if (confirm('정말로 이 상품을 삭제하시겠습니까?')) {
                var token = $("meta[name='_csrf']").attr("content");
                var header = $("meta[name='_csrf_header']").attr("content");

                $.ajax({
                    url: '/admin/item/' + itemIdForForm,
                    type: 'DELETE',
                    beforeSend: function(xhr) {
                        xhr.setRequestHeader(header, token);
                    },
                    success: function(result) {
                        alert(result);
                        location.href = '/admin/items';
                    },
                    error: function(jqXHR, status, error) {
                        alert('상품 삭제에 실패했습니다: ' + jqXHR.responseText);
                    }
                });
            }
        });
    }

    // 2. 폼 제출(저장/수정) 이벤트
    // 이 부분이 삭제 버튼 이벤트와 동일한 레벨에 있어야 합니다.
    $('form').on('submit', function(e) {
        console.log("폼 제출 이벤트 감지됨. 서버로 전송합니다.");
        e.preventDefault();  // 기본 submit 막음

        console.log("AJAX로 상품 등록 시도중");

        var formData = new FormData(this); // form의 모든 필드+파일 포함

        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");

        $.ajax({
            url: $(this).attr('action'), // form에 설정한 action 사용
            type: 'POST',
            enctype: 'multipart/form-data',
            data: formData,
            processData: false,
            contentType: false,
            beforeSend: function(xhr) {
                xhr.setRequestHeader(header, token);
            },
            success: function(result) {
                alert("상품 등록 성공!");
                location.href = '/admin/items'; // 등록 후 이동
            },
            error: function(jqXHR, status, error) {
                alert("상품 등록 실패: " + jqXHR.responseText);
            }
        });
    });
});



function bindDomEvent(){
    $(".custom-file-input").on("change", function(){
        var fileName = $(this).val().split("\\").pop();
        var fileExt = fileName.substring(fileName.lastIndexOf(".")+1);

        if(fileExt && fileExt.length > 0) {
            fileExt = fileExt.toLowerCase();
        } else {
            fileExt = "";
        }

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
