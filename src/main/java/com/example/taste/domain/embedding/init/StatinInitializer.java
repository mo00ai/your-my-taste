// package com.example.taste.domain.embedding.init;
//
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.Reader;
// import java.nio.charset.StandardCharsets;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
//
// import org.apache.commons.csv.CSVFormat;
// import org.apache.commons.csv.CSVParser;
// import org.apache.commons.csv.CSVRecord;
// import org.springframework.ai.embedding.EmbeddingModel;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.stereotype.Service;
//
// import com.example.taste.common.exception.CustomException;
// import com.example.taste.common.exception.ErrorCode;
// import com.example.taste.domain.embedding.dto.StationCsv;
// import com.example.taste.domain.embedding.entity.Station;
// import com.example.taste.domain.embedding.exception.StationError;
// import com.example.taste.domain.embedding.repository.StationRepository;
// import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeDetailResponse;
// import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeRegion;
// import com.example.taste.domain.map.dto.reversegeocode.ReverseGeocodeResult;
// import com.example.taste.domain.map.service.NaverMapService;
// import com.example.taste.domain.store.exception.StoreErrorCode;
//
// import jakarta.annotation.PostConstruct;
// import jakarta.persistence.EntityManager;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
//
// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class StatinInitializer {
// 	private final EmbeddingModel embeddingModel;
// 	private final NaverMapService naverMapService;
// 	private final StationRepository stationRepository;
// 	private final EntityManager em;
//
// 	@PostConstruct
// 	public void init() {
// 		try {
// 			// expectedStationCount: 전체 역 수, currentStationCount: 저장된 역 수
// 			int expectedStationCount = countStationsInCsv();
// 			long currentStationCount = stationRepository.count();
//
// 			if (currentStationCount > 0) {
// 				return;
// 			}
// 			// 데이터 비교
// 			if (currentStationCount >= expectedStationCount && currentStationCount > 0) {
// 				return;
// 			}
//
// 		} catch (Exception ignored) {
// 		}
//
// 		try {
// 			// 역 정보 추출
// 			List<StationCsv> allStations = parseAllStationsFromCsv();
//
// 			// 역 정보 Station 엔티티로 변환
// 			List<Station> stationEntities = processAllStations(allStations);
//
// 			// List<Station> savedStations = stationRepository.saveAll(stationEntities);
// 			// DB에 100개씩 묶음 저장
// 			saveStations(stationEntities);
//
// 		} catch (Exception e) {
// 			log.error("초기 역 로딩 중 오류 발생");
// 		}
// 	}
//
// 	//  CSV 파일에서 유효한 역 데이터의 총 개수를 계산하는 메서드
// 	private int countStationsInCsv() {
//
// 		ClassPathResource resource = new ClassPathResource("data/지하철역_GEOM (역사마스터).csv");
// 		int count = 0;
//
// 		CSVFormat format = CSVFormat.DEFAULT.builder()
// 			.setHeader()
// 			.setSkipHeaderRecord(true)
// 			.setTrim(true)
// 			.build();
//
// 		try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
// 			 CSVParser parser = new CSVParser(reader, format)) {
//
// 			for (CSVRecord record : parser) {
// 				// 실제 로딩할 때와 동일한 검증 조건을 적용
// 				String stationName = record.get("역한글명칭");
// 				String lineName = record.get("호선명칭");
//
// 				if (stationName != null && !stationName.trim().isEmpty() &&
// 					lineName != null && !lineName.trim().isEmpty()) {
// 					count++; // 유효한 데이터만 카운트
// 				}
// 			}
//
// 		} catch (IOException e) {
// 			return 0; // 오류 시 0 반환하여 로딩이 진행되도록 함
// 		}
//
// 		return count;
// 	}
//
// 	// CSV 파일에서 모든 역 정보를 읽어오는 메서드
// 	private List<StationCsv> parseAllStationsFromCsv() {
// 		ClassPathResource resource = new ClassPathResource("data/지하철역_GEOM (역사마스터).csv");
// 		List<StationCsv> allStations = new ArrayList<>();
//
// 		// CSV 읽기 설정: 헤더 인식, 공백 제거, 첫 줄 건너뛰기
// 		CSVFormat format = CSVFormat.DEFAULT.builder()
// 			.setHeader()
// 			.setSkipHeaderRecord(true)
// 			.setTrim(true)
// 			.build();
//
// 		// 자동 리소스 관리(모든 역 데이터 추출)
// 		try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
// 			 CSVParser parser = new CSVParser(reader, format)) {
//
// 			// StationCsv 객체로 변환
// 			for (CSVRecord record : parser) {
// 				String stationName = record.get("역한글명칭");
// 				String lineName = record.get("호선명칭");
//
// 				// 데이터 검증
// 				if (stationName == null || stationName.trim().isEmpty() ||
// 					lineName == null || lineName.trim().isEmpty()) {
// 					continue;
// 				}
//
// 				StationCsv stationDto = StationCsv.builder()
// 					.name(stationName)
// 					.line(lineName)
// 					.latitude(record.get("환승역Y좌표"))  // 위도
// 					.longitude(record.get("환승역X좌표"))  // 경도
// 					.build();
//
// 				allStations.add(stationDto);
// 			}
//
// 		} catch (IOException e) {
// 			throw new CustomException(ErrorCode.FILE_READ_ERROR);
// 		}
//
// 		return allStations;
// 	}
//
// 	/**
// 	 * StationCsv 목록을 Station 엔티티 목록으로 변환하는 메서드
// 	 * 각 역에 대해 주소 조회와 임베딩 벡터 생성을 수행합니다.
// 	 */
// 	private List<Station> processAllStations(List<StationCsv> stationCsvList) {
// 		List<Station> stationEntities = new ArrayList<>();
// 		int processedCount = 0;
//
// 		for (StationCsv stationDto : stationCsvList) {
// 			try {
// 				// 좌표를 사용해 행정구역 정보 조회 (네이버 지도 API 호출)
// 				ReverseGeocodeDetailResponse response = naverMapService.getAddressFromStringCoordinates(
// 					stationDto.getLongitude() + "," + stationDto.getLatitude());
// 				Map<String, String> addressInfo = extractAdministrativeArea(response);
//
// 				// Station 엔티티 생성
// 				Station station = Station.builder()
// 					.name(stationDto.getName())
// 					.line(stationDto.getLine())
// 					.latitude(stationDto.getLatitude())
// 					.longitude(stationDto.getLongitude())
// 					.sido(addressInfo.get("sido"))           // 시도 (예: 서울특별시)
// 					.sigungu(addressInfo.get("sigungu"))     // 시군구 (예: 강남구)
// 					.eupmyeondong(addressInfo.get("eupmyeondong")) // 읍면동 (예: 역삼동)
// 					.build();
//
// 				// 역명을 임베딩 벡터로 변환 ex) 서울역
// 				station.setEmbeddingVector(embeddingModel.embed(stationDto.getName() + "역"));
//
// 				stationEntities.add(station);
// 				processedCount++;
//
// 				// 진행상황 로깅
// 				if (processedCount % 100 == 0) {
// 					log.info("처리 진행률: {}/{} 완료", processedCount, stationCsvList.size());
// 				}
//
// 			} catch (Exception e) {
// 				// 실패한 역은 건너뛰고 다음 역 처리 계속
// 				throw new CustomException(StationError.STATION_SAVE_ERROR);
// 			}
// 		}
//
// 		return stationEntities;
// 	}
//
// 	private void saveStations(List<Station> stationEntities) {
// 		final int BATCH_SIZE = 100;
//
// 		int total = stationEntities.size();
//
// 		for (int start = 0; start < total; start += BATCH_SIZE) {
// 			int end = Math.min(start + BATCH_SIZE, total);
// 			List<Station> batch = stationEntities.subList(start, end);
// 			try {
// 				for (Station st : batch) {
// 					em.persist(st);
// 				}
// 				em.flush();
// 				em.clear();
// 				log.info("JPA 배치 저장 진행: {}/{}", end, total);
//
// 			} catch (Exception e) {
// 				log.error("배치 저장 실패 [{}~{}], 스킵합니다: {}", start, end, e.getMessage());
// 			}
//
// 		}
//
// 	}
//
// 	// reverse geocoding -> 행정동 주소 추출
// 	private Map<String, String> extractAdministrativeArea(ReverseGeocodeDetailResponse response) {
// 		// "admcode" 타입 결과만 사용 (보통 이게 행정동 기준)
// 		ReverseGeocodeResult admResult = response.getResults().stream()
// 			.filter(result -> "admcode".equals(result.getName()))
// 			.findFirst()
// 			.orElseThrow(() -> new CustomException(StoreErrorCode.ADMCODE_NOT_FOUND));
//
// 		ReverseGeocodeRegion region = admResult.getRegion();
//
// 		String sido = region.getArea1().getName();
// 		String sigungu = region.getArea2().getName();
// 		String eupmyeondong = region.getArea3().getName();
//
// 		return Map.of(
// 			"sido", sido,    // 시/도
// 			"sigungu", sigungu,    // 시/군/구
// 			"eupmyeondong", eupmyeondong // 읍/면/동
// 		);
// 	}
//
// 	// /**
// 	//  * StationCsv 목록을 Station 엔티티 목록으로 변환하는 메서드
// 	//  * 각 역에 대해 주소 조회와 임베딩 벡터 생성을 수행합니다.
// 	//  */
// 	// private List<Station> processAllStationsTest(List<StationCsv> stationCsvList) {
// 	// 	List<Station> stationEntities = new ArrayList<>();
// 	// 	int processedCount = 0;
// 	//
// 	// 	for (StationCsv stationDto : stationCsvList) {
// 	// 		try {
// 	// 			// 좌표를 사용해 행정구역 정보 조회 (네이버 지도 API 호출)
// 	// 			ReverseGeocodeDetailResponse response = naverMapService.getAddressFromStringCoordinates(
// 	// 				stationDto.getLongitude() + "," + stationDto.getLatitude());
// 	// 			Map<String, String> addr = storeService.extractAdministrativeArea(response);
// 	//
// 	// 			// Station 엔티티 생성
// 	// 			Station station = Station.builder()
// 	// 				.name(stationDto.getName())
// 	// 				.line(stationDto.getLine())
// 	// 				.latitude(stationDto.getLatitude())
// 	// 				.longitude(stationDto.getLongitude())
// 	// 				.sido(addr.get("sido"))           // 시도 (예: 서울특별시)
// 	// 				.sigungu(addr.get("sigungu"))     // 시군구 (예: 강남구)
// 	// 				.eupmyeondong(addr.get("eupmyeondong")) // 읍면동 (예: 역삼동)
// 	// 				.build();
// 	//
// 	// 			// 역명을 임베딩 벡터로 변환하여 저장 (검색 기능을 위해)
// 	// 			station.setEmbeddingVector(embeddingModel.embed(stationDto.getName()));
// 	//
// 	// 			stationEntities.add(station);
// 	// 			processedCount++;
// 	//
// 	// 			// 진행상황 로깅 (100개마다 출력)
// 	// 			if (processedCount % 100 == 0) {
// 	// 				log.info("CSV 처리 진행률: {}/{} 완료", processedCount, stationCsvList.size());
// 	// 			}
// 	//
// 	// 		} catch (Exception e) {
// 	// 			// 실패한 역은 건너뛰고 다음 역 처리 계속
// 	// 			throw new CustomException(StationError.STATION_SAVE_ERROR);
// 	// 		}
// 	// 	}
// 	//
// 	// 	return stationEntities;
// 	// }
// 	//
// 	// public List<Station> loadLineStationTest(String LineName) {
// 	// 	// 1) CSV에서 원하는 호선 DTO 추출 테스트용 2호선
// 	// 	List<StationCsv> csvList = parseCsvByLine(LineName);
// 	//
// 	// 	// 2) DTO → 엔티티 변환
// 	// 	List<Station> entities = new ArrayList<>();
// 	// 	for (StationCsv dto : csvList) {
// 	// 		// 행정동주소 조회
// 	// 		ReverseGeocodeDetailResponse response = naverMapService.getAddressFromStringCoordinates(
// 	// 			dto.getLongitude() + "," + dto.getLatitude());
// 	// 		Map<String, String> addr = storeService.extractAdministrativeArea(response);
// 	//
// 	// 		Station st = Station.builder()
// 	// 			.name(dto.getName())
// 	// 			.line(dto.getLine())
// 	// 			.latitude(dto.getLatitude())
// 	// 			.longitude(dto.getLongitude())
// 	// 			.sido(addr.get("sido"))
// 	// 			.sigungu(addr.get("sigungu"))
// 	// 			.eupmyeondong(addr.get("eupmyeondong"))
// 	// 			.build();
// 	// 		// 역명 임베딩
// 	// 		st.setEmbeddingVector(embeddingModel.embed(dto.getName()));
// 	// 		// 엔터티 추가
// 	// 		entities.add(st);
// 	// 	}
// 	//
// 	// 	// 3) DB에 일괄 저장
// 	// 	return stationRepository.saveAll(entities);
// 	// }
// 	//
// 	// /**
// 	//  * 클래스패스에 있는 CSV를 읽어서,
// 	//  * 지정한 호선(lineFilter)만 StationCsv DTO로 반환
// 	//  */
// 	// private List<StationCsv> parseCsvByLine(String lineFilter) {
// 	//
// 	// 	ClassPathResource resource =
// 	// 		new ClassPathResource("data/지하철역_GEOM (역사마스터).csv");
// 	// 	List<StationCsv> result = new ArrayList<>();
// 	// 	Reader reader = null;
// 	// 	CSVParser parser = null;
// 	//
// 	// 	CSVFormat format = CSVFormat.DEFAULT.builder()
// 	// 		.setHeader()
// 	// 		.setSkipHeaderRecord(true)
// 	// 		.setTrim(true)
// 	// 		.build();
// 	//
// 	// 	try {
// 	// 		reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
// 	// 		parser = new CSVParser(reader, format);
// 	// 		for (CSVRecord rec : parser) {
// 	// 			String line = rec.get("호선명칭");
// 	// 			if (!lineFilter.equals(line))
// 	// 				continue;
// 	//
// 	// 			StationCsv dto = StationCsv.builder()
// 	// 				.name(rec.get("역한글명칭"))
// 	// 				.line(rec.get("호선명칭"))
// 	// 				.latitude(rec.get("환승역Y좌표"))  // 위도
// 	// 				.longitude(rec.get("환승역X좌표"))  // 경도
// 	// 				.build();
// 	//
// 	// 			result.add(dto);
// 	// 		}
// 	// 	} catch (IOException e) {
// 	// 		throw new CustomException(ErrorCode.FILE_READ_ERROR);
// 	// 	} finally {
// 	// 		try {
// 	// 			if (parser != null)
// 	// 				parser.close();
// 	// 			if (reader != null)
// 	// 				reader.close();
// 	// 		} catch (IOException ignored) {
// 	// 		}
// 	// 	}
// 	//
// 	// 	return result;
// 	//
// 	// }
// 	//
// }
//
//
//
