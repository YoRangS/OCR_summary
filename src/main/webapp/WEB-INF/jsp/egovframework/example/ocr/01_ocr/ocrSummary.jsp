<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi">
        <title>내용요약＜OCR summary</title>
        <!-- CSS_Common UI -->
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/css/egovframework/capstone.css" />
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
                                <li><a href="#"><span class="tab_title">스캔 결과</span></a></li>
                                <!-- [D] 현재 탭 클래스 (current) -->
                                <li class="current"><a href="#"><span class="tab_title">내용 요약</span></a></li>
                                <li><a href="#"><span class="tab_title">태그 추출</span></a></li>
                            </ul>
                        </div>
                        <!-- //탭 메뉴 -->

                        <!-- 탭 컨텐츠 & 스크롤 영역 -->
                        <div class="tab_cont scroll_area">
                            <!-- 컨텐츠 타이틀 (숨김) -->
                            <h2 class="blind">내용 요약 폼</h2>
                            <!-- //컨텐츠 타이틀 (숨김) -->
                            <!-- <div class="form_view">
                                <table>
                                    <colgroup>
                                        <col style="width:176px;">
                                        <col style="width:auto;">
                                    </colgroup>
                                    <tbody>
                                        <tr>
                                            <th>스캔 파일명</th>
                                            <td>
                                                <span class="txt">${fileName}</span>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th>사용할 언어</th>
                                            <td>
                                            	<span class="txt">${lang}</span>
                                            </td>
                                        </tr>
                                        <tr>
                                        	<th>요약할 내용</th>
                                        	<td>
                                        		<div class="input_group textarea_control">
                                                    <textarea rows="8" cols="">${result}</textarea>
                                                </div>
                                        	</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div> -->
                            <!-- //폼 영역 -->

                            <!-- 하단 버튼 영역 -->
                            <!-- <div class="view_btn_area">
                                [D] 버튼 비활성화 시 disabled 속성 추가 필요
                                	기본 : <button class="btn">
                               		비활성화 : <button class="btn" disabled>

                                완료 버튼 (complete)
                                <button class="btn complete">내용요약 실행</button>
                                <input class="btn complete" type="submit" value="내용요약 실행" />
                            </div>
                            </form> -->
                            <!-- //하단 버튼 영역 -->
                            <span type="hidden" class="txt" >${fileName}</span>

                            <!-- 폼 영역 -->
                            <form action="/summary.do" method="POST">
                            <div class="form_view">
                                <table>
                                    <colgroup>
                                        <col style="width:176px;">
                                        <col style="width:auto;">
                                    </colgroup>
                                    <tbody>
                                        <tr>
                                            <th>요약 결과</th>
                                            <td>
                                                <div class="input_group textarea_control">
                                                    <textarea id="summary" name="summary"rows="8" cols="">${summary}</textarea>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <th>저장 주소</th>
                                            <td>
                                                <div class="flex_group">
                                                    <div class="input_group">
                                                        <input type="text" id="location" name="location" class="form_control" name="" value=${location}>
                                                    </div>
                                                    <!-- [D] 버튼 비활성화 시 disabled 속성 추가 필요
                                                    	기본 : <button class="btn">
                                                    	비활성화 : <button class="btn" disabled> -->
                                                    	<!-- 폼 영역 -->
                            						
                                                    	<button class="btn" type="submit">저장하기</button>
   	 													<input type="hidden" name="scanResult" value="${result}">
                                                   	
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                            <!-- //폼 영역 -->
							</form>
                            <!-- 하단 버튼 영역 -->
                            <div class="view_btn_area">
                                <!-- [D] 버튼 비활성화 시 disabled 속성 추가 필요
                                기본 : <button class="btn">
                                비활성화 : <button class="btn" disabled> -->
                                <button class="btn" onclick="history.back()">돌아가기</button>
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