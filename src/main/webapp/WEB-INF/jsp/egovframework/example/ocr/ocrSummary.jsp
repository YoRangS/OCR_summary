<%--
클래스 이름 : ocrSummary.jsp
설명 : 텍스트의 요약본을 출력하고 저장하게 한다
수정 정보
2008.08 정은총 최초 생성
2008.08 송창우 파일 경로, 파일 저장 밑 메세지 출력 추가
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>OCR Summary</title>
</head>
<body>
	<div id="main">
		<form action="/data.do" method="POST">
			<fieldset>
				<p>
					파일이름: <input type="text" name="fileTrim" value=${fileTrim}>
				</p>
				<legend>요약 결과</legend>
				<textarea id="summary" name="summary" cols="150" rows="20">${summary}</textarea>
				<br> 저장주소:<br>
				<textarea id="location" name="location" cols="150" rows="1">${location}</textarea>
				<br>
				<button type="submit">저장하기</button>
				<button type="button" onclick="history.back()">뒤로가기</button>
			</fieldset>
		</form>
		${message}
		<p>
	</div>
</body>
</html>