<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Crop Image</title>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/cropperjs/1.6.1/cropper.min.css"
	integrity="sha512-hvNR0F/e2J7zPPfLC9auFe3/SE0yG4aJCOd/qxew74NN7eyiSKjr7xJJMu1Jy2wf7FXITpWS1E/RY8yzuXN7VA=="
	crossorigin="anonymous" referrerpolicy="no-referrer" />
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/cropperjs/1.6.1/cropper.min.js"
	integrity="sha512-9KkIqdfN7ipEW6B6k+Aq20PV31bjODg4AA52W+tYtAE0jE0kMx49bjJ3FgvS56wzmyfMUHbQ4Km2b7l9+Y/+Eg=="
	crossorigin="anonymous" referrerpolicy="no-referrer"></script>
</head>
<body>
	<div id="main">
		<section>
			<form action="/cropTess.do" method="POST"
				enctype="multipart/form-data" id="imageForm">
				<fieldset>
					<legend>입력</legend>
					<label for="language">텍스트 추출에 사용할 언어를 선택해주세요:</label> <select
						id="language" name="language">
						<option value="kor">한국어</option>
						<option value="eng">영어</option>
					</select>
					<p>
						파일 선택: <input id="fileUpload" type="file" onchange="updateFileName()" /><br>
						<!-- <img id="previewImg" width="300" alt="이미지 영역" /> <br> <br> -->
						<img id="previewImage" width="500" src=""><br>
						<button type="button" id="cropBtn" onclick="crop(); return false;">자르기 시작하기</button>
						<button type="button" id="saveBtn" style="display: none">이미지 자르기</button><br>
						<br> 파일 이름: <input type="text" id="fileName" name="fileName"><br>
						<input type="hidden" type="text" id="cropImageURL" name="cropImageURL"><br>
						<br>
					<input type="submit" value="제출하기" />
				</fieldset>
			</form>
		</section>
		<button type="button" onclick="history.back()">뒤로가기</button>
	</div>
</body>
</html>

<script>
	var inputImage = document.getElementById('fileUpload');
	var saveBtn = document.getElementById('saveBtn');
	var cropBtn = document.getElementById('cropBtn');
	var previewImage = document.getElementById('previewImage');
	var cropper;
	var formData = new FormData();
	
	function updateFileName() {
	    var fileNameInput = document.getElementById('fileName');
	    var fileInput = document.getElementById('fileUpload');
	    var fileName = fileInput.files[0].name;
	    
	    // 파일 포멧을 모두 ".png" 로 바꾸기.
        fileName = fileName.replace(/\.[^/.]+$/, ".png");
	    
	    fileNameInput.value = fileName;
	}
	
	window.addEventListener('DOMContentLoaded', function () {
		previewImage = document.getElementById('previewImage');
	
		console.log("start");
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

		        cropBtn.style = style="display:none";
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
	    var fileInput = document.getElementById('fileUpload');
	
	    if (canvas) {
	        // Convert canvas to a data URL
	        var croppedImageDataURL = canvas.toDataURL("");
	
	        previewImage.src = croppedImageDataURL;
	        document.getElementById('cropImageURL').value = croppedImageDataURL;
	        cropper.destroy();
	        saveBtn.style="display:none"
	        cropBtn.style="display:block"
	    } else {
	        console.log('Canvas is null. Please adjust cropping parameters.');
	    }
	
	    console.log("before file");
	    // 파일이 선택되었는지 확인
	  if (fileInput.files.length > 0) {
		console.log("if");
	    var file = fileInput.files[0];
	
	    var imageDataURL = document.getElementById('cropImageURL').value;
	    console.log(imageDataURL);
	  }
	});
</script>