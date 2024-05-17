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
        <link rel="stylesheet" type="text/css" href="/css/egovframework/capstone.css">
        
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
                                                        <input type="file" name="add_file01" id="add_file01" size="1">
                                                        <label for="add_file01" class="btn">파일 찾기</label>
                                                    </span>
                                                </div>
                                            </td>
                                        </tr>
                                        <!-- 파일 선택 후 -->
                                        <tr>
                                            <th>파일 선택</th>
                                            <td>
                                                <!-- 첨부파일 -->
                                                <div class="flex_group attachment">
                                                    <!-- 파일 선택 후 -->
                                                    <span class="btn_file">
                                                        <!-- [D] 버튼 비활성화 시 disabled 속성 추가 필요
                                                        기본 : <input type="file">
                                                        비활성화 : <input type="file" disabled> -->
                                                        <input type="file" name="add_file02" id="add_file02" size="1" disabled>
                                                        <label for="add_file02" class="btn">파일 찾기</label>
                                                    </span>
                                                    <span class="attached_file">
                                                        <span class="file_name">cjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsVcjaqnehlsV</span><span class="file_extension">.xlsx</span>
                                                        <!-- [D] ico_btn 클릭시 파일 선택 전 상태로 출력 -->
                                                        <button class="ico_btn cancel"></button>
                                                    </span>
                                                </div>
                                                <!-- 이미지 미리보기 영역 (preview_img), 너비 300 사이즈 (small) -->
                                                <div class="preview_img small">
                                                    <img src="../../img/sample/sample.jpg" alt="첨부된 이미지 출력">
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th>텍스트 추출에 사용할 언어</th>
                                            <td>
                                                <select>
                                                    <option selected>한국어 + 영어</option>
                                                    <option>한국어</option>
                                                    <option>영어</option>
                                                </select>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th>문서 형태</th>
                                            <td>
                                                <div class="flex_group">
                                                    <select>
                                                        <option selected>일반 추출</option>
                                                        <option>특정 페이지 추출</option>
                                                    </select>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th>문서 형태</th>
                                            <td>
                                                <div class="flex_group">
                                                    <select>
                                                        <option>일반 추출</option>
                                                        <option selected>특정 페이지 추출</option>
                                                    </select>
                                                    <!-- [D] 특정 페이지 추출 선택시 출력 -->
                                                    <div class="flex_group">
                                                        <div class="input_group" style="width: 58px;">
                                                            <input type="number" class="form_control" name="">
                                                        </div>
                                                        ~
                                                        <div class="input_group" style="width: 58px;">
                                                            <input type="number" class="form_control" name="">
                                                        </div>
                                                    </div>
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
                                <button class="btn complete" disabled>텍스트 추출</button>
                            </div>
                            <!-- //하단 버튼 영역 -->
                        </div>
                        <!-- //탭 컨텐츠 & 스크롤 영역 -->
                    </div>
                    <!-- //탭 섹션 -->
                </div>
                <!-- //컨텐츠 영역 -->
            </main>
            <!-- //body -->
        </div>
        <!-- //wrapper -->
    </body>
</html>