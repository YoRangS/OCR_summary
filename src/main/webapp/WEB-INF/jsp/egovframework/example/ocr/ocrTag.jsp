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
			<form action="/vision.do" method="POST">
				<textarea id="jsonTag" name="jsonTag" cols="150" rows="20">${jsonTag}</textarea>
				<br>
				<button type="submit">의도 추출하기</button>
			</form>
			<button onclick="getTagCloud()">textCloud</button>
			<button type="button" onclick="history.back()">뒤로가기</button>
		</fieldset>
	</div>
	<%-- <p id="json" style="display:none;">jsonTag : ${jsonTag}</p> --%>
	<p id="json" style="display:none;">${jsonTag}</p>
	<div id="container" style="width: 600px; height: 400px;"></div>
	<script>
		function getTagCloud() {
			var jsonString = document.getElementById("json").textContent;
			console.log(jsonString);
			const jsonObject = JSON.parse(jsonString);
			const data = Object.entries(jsonObject).map(([key, value]) => {
			    return { x: key, value: value };
			});
			console.log(data);
			chart = anychart.tagCloud(data);
			chart.container("container");
			chart.draw();
		}
	</script>
</body>
</html>