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
        <!-- <link rel="stylesheet" type="text/css" href="../../css/capstone.css"> -->
        <style>
/*** Initialize ***/
* {margin: 0; padding: 0; box-sizing: border-box; word-break: break-all;}
html, body {width: 100%; height: 100%; font-size: 12px; overflow: hidden;}

/** 다국어 지원 폰트 **/
/* 영어 */
html:lang(en) body, html:lang(en) input, html:lang(en) textarea, html:lang(en) select, html:lang(en) button {font-family: "Segoe UI", Verdana, Arial, "맑은 고딕", "Malgun Gothic", Dotum, "MS Gothic", sans-serif;}
/* 한국어 */
html:lang(ko) body, html:lang(ko) input, html:lang(ko) textarea, html:lang(ko) select, html:lang(ko) button {font-family: "맑은 고딕", "Malgun Gothic", Dotum, "Apple SD Gothic Neo", AppleGothic, Helvetica, tahoma, sans-serif;}

textarea {white-space: pre-wrap; word-wrap: break-word; resize: none;}
table {clear: both; border-collapse: collapse;}
img, fieldset {border: 0;}
legend, caption {visibility: hidden; overflow: hidden; width: 0; height: 0; font-size: 0; line-height: 0;}
img, :before, :after {vertical-align: top;}
ul, ol {list-style: none;}
address, em {font-style: normal;}
a {text-decoration: none; cursor: pointer;}
button {background-color: transparent; border: 0; outline: 0; cursor: pointer;}
input {ime-mode: active;}
input[type="text"], input[type="number"], input[type="email"], input[type="password"], select, textarea {min-width: 45px; background-color: #fff; border: 0; color:#111; outline: none;}
input[type="text"], input[type="number"], input[type="email"], input[type="password"] {outline: none;}
input[type="checkbox"], input[type="radio"], input[type="checkbox"] + label,  input[type="radio"] + label {cursor: pointer;}
input[type="checkbox"]:disabled, input[type="checkbox"]:disabled + label, input[type="radio"]:disabled + label  {cursor: default;}
input[type=file]::file-selector-button {background-color: transparent; border: 0; color: transparent;}
:focus-visible {outline: 0;}
::-webkit-file-upload-button {cursor: pointer;}
select {height: 28px; padding: 0 23px 0 6px; background: url('../img/btn_select.png') center right no-repeat; border: 1px solid #b1b1b1; border-radius: 2px; color: #333; -webkit-appearance: none; -moz-appearance: none; appearance: none;}

/** number input 우측 화살표 숨김 **/
/* Chrome, Safari, Edge, Opera */
input::-webkit-outer-spin-button, input::-webkit-inner-spin-button {-webkit-appearance: none; margin: 0;}
/* Firefox  */
input[type='number'] {-moz-appearance: textfield;}

/** placeholder **/
/* Chrome */
::-webkit-input-placeholder {color: #a6a6a6; font-size: 13px; transition: opacity 250ms ease-in-out;}
:focus::-webkit-input-placeholder {opacity: 0.5;}
.date_input::-webkit-input-placeholder {font-size: 12px;}
/* IE 10+ */
:-ms-input-placeholder {color: #a6a6a6; transition: opacity 150ms ease-in-out;}
:focus:-ms-input-placeholder {opacity: 0.5;}
/* Firefox 19+ */
::-moz-placeholder {color: #a6a6a6; opacity: 1; transition: opacity 250ms ease-in-out;}
:focus::-moz-placeholder {opacity: 0.5;}
/* Firefox 4 - 18 */
:-moz-placeholder {color: #a6a6a6; opacity: 1; transition: opacity 250ms ease-in-out;}
:focus:-moz-placeholder {opacity: 0.5;}

::-webkit-file-upload-button {cursor: pointer;}

/* scrollbar */
::-webkit-scrollbar {width: 11px; height: 11px; padding: 2px;}
::-webkit-scrollbar-thumb {background-color: rgba(108, 110, 113, 0.3); background-clip: padding-box; border-radius: 11px; border: 2px solid transparent;}
::-webkit-scrollbar-thumb:hover {background-color: rgba(108, 110, 113, 0.7); border-width: 1px;}
::-webkit-scrollbar-track {background-color: transparent; border-radius: 11px;}
::-webkit-scrollbar-corner {background-color: transparent;}
/*** // Initialize ***/

/* 숨김 요소 */
.blind {position: absolute; overflow: hidden; width: 1px; height: 1px; margin: -1px; clip: rect(0, 0, 0, 0);}

/* 표시 타입 */
.display_none, .dsp_none {display: none !important; height: 0 !important; width:0 !important; margin:0 !important; padding: 0 !important;}
.display_none *, .dsp_none * {display: none !important; height: 0 !important; width:0 !important; margin:0 !important; padding: 0 !important;}
.display_block, .dsp_block {display: block !important;}
.display_inline_block, .dsp_inline_block {display: inline-block !important;}

/* 서브 타이틀 */
.sub_title {margin-bottom: 6px;}
.sub_title .title {max-width: 100%; font-size: 12px; line-height: 18px; font-weight: bold; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; word-break: break-all;}

/* 탭 메뉴 */
.tab_area {width: 100%; overflow: hidden;}
.tab_area ul {position: relative; display: block; width: 100%; height: 42px; border-bottom: 1px solid #202020; font-size: 0;}
.tab_area ul li {display: inline-block; width: 200px; margin-left: 1px; text-align: center; vertical-align: top;}
.tab_area ul li:first-child {margin-left: 0;}
.tab_area ul li a {display: block; height: 41px; padding: 0 10px; background-color: #f7f7f7; border: 1px solid #ddd; border-bottom: 0; border-radius: 2px 2px 0 0; color: #999;}
.tab_area ul li a .tab_title {display: inline-block; max-width: 100%; height: 41px; line-height: 41px; font-size: 15px; font-weight: bold; letter-spacing: -0.7px; vertical-align: middle; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; word-break: break-all;}
.tab_area ul li:not(.current) a:hover {background-color: #474747; border-color: #474747; color: #fff;}
.tab_area ul li:not(.current) a:active {background-color: #000; border-color: #000; color: #fff;}
.tab_area ul li.current {position: relative;}
.tab_area ul li.current a {position: relative; height: 42px; background-color: #fff; border-color: #202020; color: #202020; z-index: 2;}
/* 탭 컨텐츠 */
.tab_cont {width: 100%; padding-top: 20px;}
/* 탭 섹션 */
.tab_section {display: grid; grid-template-rows: 42px auto; width: 100%; height: 100%; overflow: hidden;}

/* 버튼 */
.btn {display: inline-block; height: 28px; margin: 0 4px; padding: 0 10px; border-radius: 2px; font-size: 12px; line-height: 26px;}
.btn:not(:disabled):not(.complete) {background-color: #fff; border: 1px solid #c2c2c2; color: #4c4c4c;}
.btn:not(:disabled):not(.complete):hover {background-color: #f5f5f5;}
.btn:not(:disabled):not(.complete):active {background-color: #fff; border-color: #969696; color: #333;}
.btn:disabled {background-color: #fff; border-color: #c2c2c2; color: #969696; cursor: default;}
.btn:first-child {margin-left: 0;}
.btn:last-child {margin-right: 0;}
.btn.complete {background-color: #202020; border: 1px solid transparent; color: #fff;}
.btn.complete:not(:disabled):hover {background-color: #474747;}
.btn.complete:not(:disabled):active {background-color: #000;}
.btn.complete:disabled {background-color: #ccc;}

/* 파일 버튼 */
.btn_file {position: relative; overflow: hidden;}
.btn_file input[type="file"] {position: absolute; right: 0; top: 0; height: 100%; opacity: 0; cursor: pointer;}
.btn_file input:disabled + .btn {background-color: #fff !important; border-color: #c2c2c2 !important; color: #969696 !important; cursor: default;}
.btn_file .btn {position: relative; cursor: pointer; z-index: 2;}

/* 아이콘 버튼 */
.ico_btn {display: inline-block; width: 16px; height: 16px; font-size: 0; vertical-align: middle;}
.ico_btn.cancel {width: 28px; height: 28px; background: url('../img/bg_del.png') center center no-repeat; font-size: 0; vertical-align: top;}

/* 첨부된 파일 */
.attached_file {display: flex; align-items: center; height: 28px; overflow: hidden;}
.attached_file .file_name {flex: 1; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;}
.attached_file .file_extension {flex-shrink: 0; display: inline-block;}
.attached_file .ico_btn {flex-shrink: 0;}

/* 첨부된 이미지 미리보기 */
.preview_img {height: fit-content; margin-top: 8px; border: 1px solid #c2c2c2; overflow: hidden;}
.preview_img img {width: 100%; vertical-align: top;}
.preview_img.small {width: 302px;}
.preview_img.medium {width: 502px;}
.preview_img.full {width: 100%;}

/* input_group */
.input_group {position: relative; padding: 0 6px; border: 1px solid #c2c2c2; background-color: #fff;}
.input_group input.form_control {width: 100%; height: 26px; line-height: 26px;}
.input_group.textarea_control {padding: 0;}
.input_group.textarea_control textarea {width: 100%; padding: 4px 6px; line-height: 18px; vertical-align: top;}

/* flex_group */
.flex_group {display: flex; align-items: center; overflow: hidden;}
.flex_group .btn {margin: 0;}
.flex_group .btn, .flex_group .btn_file, .flex_group .ico_btn {flex-shrink: 0;}
.flex_group .input_group {width: 100%;}

/* define_info */
.define_info {display: flex; column-gap: 8px; min-height: 34px; padding: 8px 0;}
.define_info .title, .define_info .data {font-size: 13px; line-height: 18px; color: #333;}
.define_info .title {flex-shrink: 0; width: 72px;}

/* toggle */
.toggle {position: relative; display: inline-block; width: 160px; height: 28px;}
.toggle input {position: absolute; opacity: 0;}
.toggle label {position: relative; display: block; width: 100%; height: 100%; padding: 4px 10px 0; background-color: #fff; border: 1px solid #b1b1b1; font-size: 12px; line-height: 16px; color: #333; text-align: center;}
.toggle input:checked + label {background-color: #ebf1fc; border-color: #3858ed; color: #3858ed; z-index: 2;}
.toggle input:disabled + label {background-color: #fff; border-color: #c2c2c2; color: #969696; cursor: default;}

/* toggle_group */
.toggle_group {overflow: hidden;}
.toggle_group .toggle {float: left; margin-left: -1px;}
.toggle_group .toggle:first-child {margin-left: 0;}
.toggle_group .toggle:first-child label {border-radius: 2px 0 0 2px;}
.toggle_group .toggle:last-child label {border-radius: 0 2px 2px 0;}

/* 폼 상세 */
.form_view {width: 100%;}
.form_view table {width: 100%; border-top: 1px solid #ccc; border-bottom: 1px solid #e3e3e3; table-layout: fixed;}
.form_view table th {padding: 8px 0 8px 15px; background-color: #fafafa; border-bottom: 1px solid #e3e3e3; color: #333; font-weight: normal; text-align: left; vertical-align: middle; letter-spacing: -0.7px;}
.form_view table td {position: relative; min-height: 28px; padding: 3px 5px 3px 14px; border-top: 1px solid #e3e3e3; font-size: 13px; color: #333; overflow: hidden;}
.form_view table td .txt {line-height: 28px; vertical-align: middle;}
.form_view table td .caution {line-height: 28px; font-weight: bold; vertical-align: middle;}
.form_view table tr:first-child td {border-top: 0;}

.form_view .input_group:only-child {max-width: 1000px;}
.form_view .input_group.textarea_control {max-width: 1000px;}
.form_view .flex_group {max-width: 1000px; column-gap: 6px;}
.form_view .flex_group.attachment {max-width: 100%;}

.form_view + .form_view {margin-top: 16px;}
.form_view + .sub_title {margin-top: 46px;}
.form_view + .toggle_group {margin-top: 46px;}

/* 폼 상세 하단 버튼 영역 */
.view_btn_area {position: relative; padding: 16px 0 30px;}

/* 레이아웃 */
.wrapper {display: grid; grid-template-rows: 58px auto; width: 100vw; height: 100vh; overflow: hidden;}
.header {width: 100%; height: 59px; padding: 16px 32px 0; background-color: #fff; border-bottom: 1px solid #bfbfbf;}
.header h1.area_title {font-size: 19px; font-weight: normal; color: #111; letter-spacing: -1px;}

/* 컨텐츠 영역 */
.body {width: 100%; overflow: hidden;}
.body .container_area {position: relative; width: 100%; min-width: 1280px; height: 100%; padding: 30px 30px 0;}

/* 컨테츠 스크롤 영역 */
.scroll_area {height: 100%; overflow-y: auto;}
        </style>
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