<%--
클래스 이름 : ocrSampleList.jsp
설명 : 이미지를 입력받아 텍스트를 출력한다
수정 정보
2008.08 정은총 최초 생성
2008.08 송창우 언어 입력, 파일 경로 관련 추가
--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>OCR Scan</title>
</head>
<body>
	<div id="main">
		<section>
			<form action="/test.do" method="POST" enctype="multipart/form-data">
				<fieldset>
					<legend>입력</legend>
					<label for="language">텍스트 추출에 사용할 언어를 선택해주세요:</label> <select
						id="language" name="language">
						<option value="kor">한국어</option>
						<option value="eng">영어</option>
					</select>
					<p>
						파일 선택: <input id="fileUpload" type="file" name="file" /><br>
						<img id="previewImg" width="300" alt="이미지 영역" /> <br> <br>
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
						<br>
						<br> <input type="submit" value="제출하기" />
				</fieldset>
			</form>
			<br>
			<hr>
			<br>
			<fieldset>
				<legend>스캔 결과</legend>
				<p>스캔한 파일: ${fileName}</p>
				<fieldset>
					<legend>오타수정 요청전</legend>
					<textarea cols="150" rows="20">${scan}</textarea>
				</fieldset>
				<form action="/summary.do" method="POST">
					<fieldset>
						<legend>오타수정 요청 후</legend>
						<p>
							스캔한 파일: <input type="text" id="fileName" name="fileName"
								value=${fileName}>
						</p>
						요약에 사용할 언어: <input type="text" id="lang" name="lang" value=${lang}>
						<p>kor = 한글, eng = 영어
						<p>
							<textarea id="sr" name="scanResult" cols="150" rows="20">${result}</textarea>
							<br>
					</fieldset>
					<input type="submit" value="요약하기" />
				</form>
			</fieldset>
		</section>
	</div>
</body>
</html>