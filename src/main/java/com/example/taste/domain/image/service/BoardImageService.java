package com.example.taste.domain.image.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.BoardImageRepository;
import com.example.taste.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
@RequiredArgsConstructor
public class BoardImageService {

	private final ImageService imageService;
	private final ImageRepository imageRepository;
	private final BoardImageRepository boardImageRepository;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;

	@Value("${cloud.aws.region.static}")
	private String region;

	private final S3Client s3Client;

	//aws에 사진 업로드 후 db에 저장
	public void saveBoardImages(List<MultipartFile> files, Long boardId) {

		//board 유효성 검사 board NOT FOUND 예외처리 필요

		List<String> uploadedKey = new ArrayList<>(); // 업로드 성공한 키 저장
		try {// db에 저장 실패 or aws 에 업로드 실패 시 에러
			imageService.uploadFile(files).forEach(data -> {
				String url = (String)data.get("url");
				String originalName = (String)data.get("originalFilename");
				String uploadName = (String)data.get("uploadFilename");
				String extension = (String)data.get("fileExtension");
				Long size = (Long)data.get("fileSize");

				Image image = Image.builder()
					.type(ImageType.BOARD)
					.url(url)
					.originFileName(originalName)
					.uploadFileName(uploadName)
					.fileExtension(extension)
					.build();

				// BoardImage boardImage = BoardImage.builder()
				// 	.board(board)
				// 	.image(image)
				// 	.build();

				imageRepository.save(image);
				// boardImageRepository.save(boardImage);

				uploadedKey.add(uploadName);
			});
		} catch (Exception e) {
			for (String key : uploadedKey) {
				DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder() // aws 에 이미 올라간 이미지 삭제
					.bucket(bucketName)  // 연결 된 대상 버킷 이름
					.key(key)  // 버킷 내 삭제할 객체 키
					.build();
				s3Client.deleteObject(deleteObjectRequest);
			}
			throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED); // 리뷰 저장 롤백위해 오류 날림
		}
	}

	public List<ImageResponseDto> findBoardImages(Long boardId) {

		//boardId 검증 예외처리 필요

		return boardImageRepository.findAllById(boardId);
	}

}
