package com.example.taste.domain.image.service;

import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.dto.ImageResponseDto;
import com.example.taste.domain.image.entity.BoardImage;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.image.repository.BoardImageRepository;
import com.example.taste.domain.image.repository.ImageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardImageService {

	private final ImageService imageService;
	private final ImageRepository imageRepository;
	private final BoardImageRepository boardImageRepository;
	private final BoardRepository boardRepository;

	//aws에 사진 업로드 후 db에 저장
	// public void saveBoardImages(List<MultipartFile> files, Long boardId) {
	//
	// 	//board 유효성 검사 board NOT FOUND 예외처리 필요
	//
	// 	List<String> uploadedKey = new ArrayList<>(); // 업로드 성공한 키 저장
	// 	try {// db에 저장 실패 or aws 에 업로드 실패 시 에러
	// 		imageService.uploadFile(files).forEach(data -> {
	// 			String url = (String)data.get("url");
	// 			String originalName = (String)data.get("originalFilename");
	// 			String uploadName = (String)data.get("uploadFilename");
	// 			String extension = (String)data.get("fileExtension");
	// 			Long size = (Long)data.get("fileSize");
	//
	// 			Image image = Image.builder()
	// 				.type(ImageType.BOARD)
	// 				.url(url)
	// 				.originFileName(originalName)
	// 				.uploadFileName(uploadName)
	// 				.fileExtension(extension)
	// 				.build();
	//
	// 			// BoardImage boardImage = BoardImage.builder()
	// 			// 	.board(board)
	// 			// 	.image(image)
	// 			// 	.build();
	//
	// 			imageRepository.save(image);
	// 			// boardImageRepository.save(boardImage);
	//
	// 			uploadedKey.add(uploadName);
	// 		});
	// 	} catch (Exception e) {
	// 		for (String key : uploadedKey) {
	// 			DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder() // aws 에 이미 올라간 이미지 삭제
	// 				.bucket(bucketName)  // 연결 된 대상 버킷 이름
	// 				.key(key)  // 버킷 내 삭제할 객체 키
	// 				.build();
	// 			s3Client.deleteObject(deleteObjectRequest);
	// 		}
	// 		throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED); // 리뷰 저장 롤백위해 오류 날림
	// 	}
	// }

	@Transactional
	public void saveBoardImages(Board board, List<MultipartFile> files) throws IOException {

		for (MultipartFile file : files) {
			Image image = imageService.saveImage(file, ImageType.BOARD);

			BoardImage boardImage = BoardImage.builder()
				.board(board)
				.image(image)
				.build();

			boardImageRepository.save(boardImage);
		}
	}

	@Transactional(readOnly = true)
	public List<ImageResponseDto> findBoardImages(Long boardId) {

		Board board = boardRepository.findById(boardId)
			.orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));

		return boardImageRepository.findAllById(boardId);
	}

	@Transactional
	public void updateBoardImages(Board board, List<Long> keepImageIds, List<MultipartFile> newImages) throws
		IOException {

		List<BoardImage> toExistImages = boardImageRepository.findImagesByBoard(board);

		// 삭제할 이미지 조회
		List<BoardImage> toDeleteImages = toExistImages.stream()
			.filter(image -> !keepImageIds.contains(image.getImage().getId()))
			.toList();

		// 삭제
		for (BoardImage bi : toDeleteImages) {
			imageService.deleteImage(bi.getImage());
		}
		boardImageRepository.deleteAll(toDeleteImages);

		// 새 이미지 추가
		for (MultipartFile file : newImages) {
			Image image = imageService.saveImage(file, ImageType.BOARD);

			BoardImage boardImage = BoardImage.builder()
				.board(board)
				.image(image)
				.build();

			boardImageRepository.save(boardImage);
		}
	}

	@Transactional
	public void deleteBoardImages(Board board) {

		List<BoardImage> boardImages = boardImageRepository.findImagesByBoard(board);
		for (BoardImage bi : boardImages) {
			imageService.deleteImage(bi.getImage());
		}

		boardImageRepository.deleteAll(boardImages);
	}

}
