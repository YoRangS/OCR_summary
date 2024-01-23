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
		            <option value="kor">한국어</option>
		            <option value="eng">영어</option>
		        </select>
		        <p>
					파일 선택: <input id="fileUpload" type="file"/><br>
		            <!-- <img id="previewImg" width="300" alt="이미지 영역" /> <br> <br> -->
		            <img id="previewImage" src=""><br>
		            <button type="button" onclick="crop(); return false;">Cropped Image</button>
            		<button type="button" id="saveBtn" style="display:none">Save</button>
		            <input type="hidden" type="file" id="croppedImage" name="file">
		            <script
			            src="https://cdnjs.cloudflare.com/ajax/libs/cropperjs/1.6.1/cropper.min.js"
			            integrity="sha512-9KkIqdfN7ipEW6B6k+Aq20PV31bjODg4AA52W+tYtAE0jE0kMx49bjJ3FgvS56wzmyfMUHbQ4Km2b7l9+Y/+Eg=="
			            crossorigin="anonymous" referrerpolicy="no-referrer"></script>
				    <script>
				        var inputImage = document.getElementById('fileUpload');
				        var saveBtn = document.getElementById('saveBtn');
				        var previewImage = document.getElementById('previewImage');
				        var cropper;
				        
				
				        window.addEventListener('DOMContentLoaded', function () {
				        	previewImage = document.getElementById('previewImage');
				
				            inputImage.addEventListener('change', function (e) {
				                var file = e.target.files[0];
				                var reader = new FileReader();
				
				                reader.onload = function (event) {
				                	previewImage.src = event.target.result;
				                };
				
				                reader.readAsDataURL(file);
				            });
				        });
				
				        function crop() {
				            // 파일이 선택되었는지 확인
				            if (inputImage.files && inputImage.files[0]) {
				                // 파일을 읽기 위한 FileReader 객체 생성
				                var reader = new FileReader();
				
				                // 파일이 로드되었을 때의 이벤트 처리
				                reader.onload = function (e) {
				                    console.log("file loaded!");
				
				                    // Destroy the previous cropper instance before creating a new one
				                    if (cropper) {
				                        cropper.destroy();
				                    }
				
				                    cropper = new Cropper(previewImage, {
				                        viewMode: 1,
				                        dragMode: 'move',
				                        autoCropArea: 1, // Set to 1 for auto-crop
				                        cropBoxResizable: true, // Allow resizing
				                        cropBoxMovable: true // Allow moving
				                    });
				                };
				
				                // 파일을 읽어 데이터 URL로 변환
				                reader.readAsDataURL(inputImage.files[0]);
				
				                saveBtn.style = style="display:inline";
				            } else {
				                alert('Please select an image file.');
				            }
				        }
				
				        saveBtn.addEventListener('click', function () {
				            var canvas = cropper.getCroppedCanvas();
				            var previewImage = document.getElementById('previewImage');
				
				            if (canvas) {
				                // Convert canvas to a data URL
				                var croppedImageDataURL = canvas.toDataURL("");
				
				                previewImage.src = croppedImageDataURL;
				                document.getElementById('croppedImage').value = croppedImageDataURL;
				                cropper.destroy();
				                saveBtn.style="display:none"
				            } else {
				                console.log('Canvas is null. Please adjust cropping parameters.');
				            }
				        });
				        
				        function submitHandler(e) {
				        /* document.getElementById('imageForm').addEventListener('submit', function (e) { */
				        	  e.preventDefault(); // 폼의 기본 동작을 막음
				        	  
				        	  // FormData 객체 생성
			        	      var formData = new FormData();

				        	  var fileInput = document.getElementById('fileUpload');

				        	  // 파일이 선택되었는지 확인
				        	  if (fileInput.files.length > 0) {
				        	    var file = fileInput.files[0];

				        	    /* // FileReader를 사용하여 Data URL 읽기
				        	    var reader = new FileReader();
				        	    reader.onload = function (event) { */
				        	      var imageDataURL = document.getElementById('croppedImage').value;
				        	      console.log("dataurl input!");
		
				        	      // Data URL을 Blob 객체로 변환
				        	      var blob = dataURLtoBlob(imageDataURL);
		
				        	      formData.append('file', blob, 'image.png');
				        	      console.log(blob);
				        	      console.log("file blob");
				        	    /*};  */
				        	    /* console.log("before reader");
				        	    reader.readAsDataURL(file);
				        	    console.log("after reader"); */
				        	  }
				        	  
				        	  formData.append('language', document.getElementById('language').value);
				        	  formData.append('tessType', document.getElementById('tessType').value);
				        	  formData.append('startPage', document.getElementById('startPage').value);
				        	  formData.append('endPage', document.getElementById('endPage').value);
				        	  console.log("append");
				        	  console.log(document.getElementById('language').value);
				        	  
				        	  // 폼을 서버로 제출
			        	      submitForm(formData);
				        	 	
			        	   // 이벤트 리스너 제거 (또는 주석 처리)
			        	      document.getElementById('imageForm').removeEventListener('submit', submitHandler);
				        /* }); */
				        }
				        
				     	// 제출 버튼에 이벤트 리스너 등록
				        document.getElementById('imageForm').addEventListener('submit', submitHandler);
				        
			        	function submitForm(formData) {
			        	  // Ajax를 사용하여 FormData를 서버로 전송
			        	  var xhr = new XMLHttpRequest();
			        	  xhr.open('POST', '/tess.do', true);
			        	  xhr.onreadystatechange = function () {
			        	    if (xhr.readyState === 4 && xhr.status === 200) {
			        	      // 서버 응답 처리
			        	      console.log(xhr.responseText);
			        	      /* window.location.href = '/tess.do'; */
			        	    }
			        	  };
			        	  xhr.send(formData);
			        	}

			        	function dataURLtoBlob(dataURL) {
			        	  var arr = dataURL.split(',');
			        	  var mime = arr[0].match(/:(.*?);/)[1];
			        	  var bstr = atob(arr[1]);
			        	  var n = bstr.length;
			        	  var u8arr = new Uint8Array(n);

			        	  while (n--) {
			        	    u8arr[n] = bstr.charCodeAt(n);
			        	  }

			        	  return new Blob([u8arr], { type: mime });
			        	}
				    </script>
		            <!-- <script>
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
		            </script> -->
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