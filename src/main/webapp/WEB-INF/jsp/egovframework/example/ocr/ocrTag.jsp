<%--
클래스 이름 : ocrTag.jsp
설명 : 텍스트 원문으로 json 형식의 태그를 출력하고 wordCloud와 그래프를 생성한다.
수정 정보
2008.11.14 정은총 최초 생성
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://cdn.anychart.com/releases/8.11.1/js/anychart-core.min.js"></script>
<script src="https://cdn.anychart.com/releases/8.11.1/js/anychart-tag-cloud.min.js"></script>
<title>OCR Tag</title>
</head>
<body>
	<div id="main">
		<fieldset>
			<legend>태그 추출 결과</legend>
			<form action="/purpose.do" method="POST">
				 언어: <input type="text" id="lang" name="lang" value=${lang}>
				<p>kor = 한글, eng = 영어
				원본 텍스트<br>
				<textarea id="sr" name="scanResult" cols="150" rows="20">${result}</textarea>
				<br>태그<br>
				<textarea id="jsonTag" name="jsonTag" cols="150" rows="10">${jsonTag}</textarea>
				<br>
				<button type="submit">의도 추출하기</button>
			</form>
			<textarea id="pur" name="purpose" cols="150" rows="5">${purpose}</textarea>
			<br>
			<button onclick="window.open('${imgLink}', '_blank')">WordCloud</button>
			<button type="button" onclick="history.back()">뒤로가기</button>
		</fieldset>
	</div>
</body>
</html>