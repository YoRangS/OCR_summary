<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi">
        <title>파일선택＜OCR summary</title>
        <!-- CSS_Common UI -->
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/css/egovframework/capstone.css" />
		<!--jquery 사용.-->
		<!-- 탭 클릭 이벤트 - 활성화된 탭의 li태그에 "current" class 추가-->
		<script type="text/javascript" language="javascript" src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
		<script>
			$(document).ready(function(){
				$(".tab").click(function(){
					var tab = $(this).attr("href");
					if(tab=="#tab1"){
						$(".tab1").addClass("current");
						$(".tab2").removeClass("current");
					}else if(tab=="#tab2"){
						$(".tab1").removeClass("current");
						$(".tab2").addClass("current");
					}
				});
			});
		</script>
    </head>
    <body>
        <!-- wrapper -->
        <div class="wrapper">
            <!-- header -->
            <header class="header">
                <!-- Area Title -->
                <h1 class="area_title">OCR summary</h1>
            </header>
            <!-- //header -->

            <!-- body -->
            <form action="/tess.do" method="POST" enctype="multipart/form-data" id="imageForm">
            <main class="body">
                <!-- 컨텐츠 영역 -->
                <div class="container_area">
                    <!-- 탭 섹션 -->
                    <div class="tab_section">
                        <!-- 탭 메뉴 -->
                        <div class="tab_area">
                            <ul>
                                <!-- [D] 현재 탭 클래스 (current) -->
                                <li class="current tab1"><a href="#tab1" class="tab"><span class="tab_title">파일 선택</span></a></li>
                                <li class="tab2"><a href="#tab2" class="tab"><span class="tab_title">이미지 자르기</span></a></li>
                            </ul>
                        </div>
                        <!-- //탭 메뉴 -->

                        <!-- 탭 컨텐츠 & 스크롤 영역 -->
                        <div class="tab_cont scroll_area">
                            <!-- 컨텐츠 타이틀 (숨김) -->
                            <h2 class="blind">파일 선택 폼</h2>
                            <!-- //컨텐츠 타이틀 (숨김) -->

                            <!-- 폼 영역 -->
                            <div class="form_view">
                                <table>
                                    <colgroup>
                                        <col style="width:176px;">
                                        <col style="width:auto;">
                                    </colgroup>
                                    <tbody>
                                        <!-- 파일 선택 전 -->
                                        <tr>
                                            <th>파일 선택</th>
                                            <td>
                                                <!-- 첨부파일 -->
                                                <div class="flex_group attachment">
                                                    <!-- 파일 선택 전 -->
                                                    <span class="btn_file">
                                                        <!-- [D] 버튼 비활성화 시 disabled 속성 추가 필요
                                                        기본 : <input type="file">
                                                        비활성화 : <input type="file" disabled> -->
                                                        <input type="file" name="file" id="fileUpload" size="1">
                                                        <label for="fileUpload" class="btn">파일 찾기</label>
                                                        <script>
                                                        	const fileInput = document.getElementById("fileUpload");
                                                		
                                		                	const handleFiles = (e) => {
                                		                    	const selectedFile = [...fileInput.files];
                                		                    	const fileReader = new FileReader();
                                		
                                		                    	fileReader.readAsDataURL(selectedFile[0]);
                                		
                                		                    	fileReader.onload = function () {
                                		                        	document.getElementById("previewImg").src = fileReader.result;
                                		                    	};
                                		                	};
                                		
                                		                	fileInput.addEventListener("change", handleFiles);
                                                        </script>
                                                    </span>
                                                </div>
                                                <div class="preview_img small">
                                                    <img id="previewImg" width="300" alt="이미지 영역">
                                                </div>
                                            </td>
                                        </tr>
                                            <th>텍스트 추출에 사용할 언어</th>
                                            <td>
                                                <select id="language" name="language">
                                                    <option selected value="kor+eng">한국어 + 영어</option>
                                                    <option value="kor">한국어</option>
                                                    <option value="eng">영어</option>
                                                </select>
                                            </td>
                                        </tr>
                                            <th>문서 형태</th>
                                            <td>
                                                <div class="flex_group">
                                                    <select id="tessType" name="tessType">
                                                        <option selected value="tess">일반 추출</option>
                                                        <option value="tessLimit">특정 페이지 추출</option>
                                                    </select>
                                                    <!-- [D] 특정 페이지 추출 선택시 출력 -->
                                                    <div class="flex_group" id="tessLimitPage" style="display:none;">
                                                        <div class="input_group">
                                                        	<label for="startPage">시작 페이지:</label>
                                                            <input type="number" id="startPage"  name="startPage" value="1" style="width: 50px;">
                                                        </div>
                                                        ~
                                                        <div class="input_group" >
                                                        	<label for="endPage">종료 페이지:</label>
                                                            <input type="number" id="endPage" name="endPage" value="1" style="width: 50px;">
                                                        </div>
                                                    </div>
                                                    <script>
													    const extractTypeSelect = document.getElementById("tessType");
													    const specificPageFields = document.getElementById("tessLimitPage");
													
													    extractTypeSelect.addEventListener("change", function() {
													    	specificPageFields.style.display = (extractTypeSelect.value === "tessLimit") ? "block" : "none";
													    });
													</script>
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                            <!-- //폼 영역 -->

                            <!-- 하단 버튼 영역 -->
                            <div class="view_btn_area">
                                <!-- [D] 버튼 비활성화 시 disabled 속성 추가 필요
                                기본 : <button class="btn">
                                비활성화 : <button class="btn" disabled> -->

                                <!-- 완료 버튼 (complete) -->
                                <button type="submit" class="btn complete">텍스트 추출</button>
                            </div>
                            <!-- //하단 버튼 영역 -->
                        </div>
                        <!-- //탭 컨텐츠 & 스크롤 영역 -->
                    </div>
                    <!-- //탭 섹션 -->
                </div>
                <!-- //컨텐츠 영역 -->
            </main>
            </form>
            <!-- //body -->
        </div>
        <!-- //wrapper -->
    </body>
</html>