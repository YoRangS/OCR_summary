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
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/cropperjs/1.6.1/cropper.min.css"
        integrity="sha512-hvNR0F/e2J7zPPfLC9auFe3/SE0yG4aJCOd/qxew74NN7eyiSKjr7xJJMu1Jy2wf7FXITpWS1E/RY8yzuXN7VA=="
        crossorigin="anonymous" referrerpolicy="no-referrer" />
</head>
<body>
	<div id="main">
		<section>
		<form action="/tess.do" method="POST" enctype="multipart/form-data" id="imageForm">
		    <fieldset>
		        <legend>입력</legend>
		        <label for="language">텍스트 추출에 사용할 언어를 선택해주세요:</label>
		        <select id="language" name="language">
		        	<option value="kor+eng">한국어+영어</option>
		            <option value="kor">한국어</option>
		            <option value="eng">영어</option>
		        </select>
		        <p>
		        <button type="button" onclick="redirectToOcrCropPage();">이미지 자르기</button>
		        <br> ※이미지 자르기를 하려고 할 경우 파일을 새로 입력하여야 합니다
		        <script>
				    function redirectToOcrCropPage() {
				        // Redirect to the ocrCrop.jsp page
				        window.location.href = "/goToCrop.do";
				    }
				</script>
		        <p>
					파일 선택: <input id="fileUpload" type="file" name="file"/><br>
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
		            <img id="previewImg" width="300" alt="이미지 영역" /> <br> <br>
            		<button type="button" id="saveBtn" style="display:none">Save</button>
		            <input type="hidden" type="text" id="croppedImage" name="file">
		            <br>
		            <br>
		            <label>추출 방식 선택:</label>
		            <select id="tessType" name="tessType">
		                <option value="tess">일반 추출</option>
		                <option value="tessLimit">특정 페이지 추출</option>
		            </select>
		
		            <!-- Check for specific page extraction -->
		            <div id="specificPageFields" style="display:none;">
		                <label for="startPage">시작 페이지:</label>
		                <input type="text" id="startPage" name="startPage" value="1" />
		
		                <label for="endPage">종료 페이지:</label>
		                <input type="text" id="endPage" name="endPage" value="1" />
		            </div>
		            
		            <br><br>
		            <input type="submit" value="제출하기" />
		    </fieldset>
		</form>
		
		<script>
		    const extractionTypeSelect = document.getElementById("tessType");
		    const specificPageFields = document.getElementById("specificPageFields");
		
		    extractionTypeSelect.addEventListener("change", function() {
		        specificPageFields.style.display = (extractionTypeSelect.value === "tessLimit") ? "block" : "none";
		    });
		</script>
			<br>
			<hr>
			<br>
			<fieldset>
				<legend>스캔 결과</legend>
				<p>스캔한 파일: ${fileName}</p>
				<fieldset>
					<legend>오타수정 요청 전</legend>
					<textarea cols="150" rows="20">${scan}</textarea>
				</fieldset>
				<fieldset>
					<form action="/summary.do" method="POST">
						<legend>오타수정 요청 후!!!</legend>
						<p>
							스캔한 파일: <input type="text" id="fileName" name="fileName"
								value=${fileName}>
						</p>
						요약에 사용할 언어: <input type="text" id="lang" name="lang" value=${lang}>
						<p>kor = 한글, eng = 영어
						<p>
							<textarea id="sr" name="scanResult" cols="150" rows="20">${result}</textarea>
							<br>
					<input type="submit" value="요약하기" />
					</form><br>
					<form action="/tag.do" method="POST">
						<!-- <input type="text" name="scanResult" style="display:none;" value=${result} /> -->
						<%-- <p name="scanResult" style="display:none;">${result}</p> --%>
						<textarea id="sr" name="scanResult" cols="150" rows="20" style="display:none;">${result}</textarea>
						<input type="text" name="lang" style="display:none;" value=${lang} />
						<input type="submit" value="태그 추출하기" />
					</form>
				</fieldset>
			</fieldset>
		</section>
	</div>
</body>
</html>