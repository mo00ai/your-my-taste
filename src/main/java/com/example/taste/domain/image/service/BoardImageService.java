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
	public void deleteBoardImages(Board board) {

		List<BoardImage> boardImages = boardImageRepository.findImagesByBoard(board);
		for (BoardImage bi : boardImages) {
			imageService.deleteImage(bi.getImage());
		}

		boardImageRepository.deleteAll(boardImages);
	}

}
