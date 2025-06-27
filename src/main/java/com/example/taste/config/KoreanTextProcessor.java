package com.example.taste.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.stereotype.Component;

import com.example.taste.domain.store.init.CategoryManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import scala.collection.Seq;

@Slf4j
@Component
@RequiredArgsConstructor
public class KoreanTextProcessor {
	private final CategoryManager categoryManager;

	@PostConstruct
	public void initialize() {
		String text = "초기화";
		normalize(text);
		extractSearchKeywords("초기화");

		log.info("OKT 형태소 분석기 초기화 ");
	}

	/**
	 * 텍스트 정규화
	 */
	public String normalize(String text) {
		if (text == null || text.trim().isEmpty()) {
			log.info("텍스트 정규화 중 오류 발생: {}", "빈 문자열");
			return "";
		}

		try {
			CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
			return normalized.toString();
		} catch (Exception e) {
			log.error("텍스트 정규화 중 오류 발생: {}", e.getMessage());
			return text;
		}
	}

	/**
	 * 토큰화 (모든 형태소)
	 */
	public List<String> tokenize(String text) {
		if (text == null || text.trim().isEmpty()) {
			log.info("텍스트 토큰화 중 오류 발생: {}", "빈 문자열");
			return Collections.emptyList();
		}

		try {
			// 텍스트 정규화
			CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
			// 토크나이즈
			Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
			return OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens);
		} catch (Exception e) {
			log.error("토큰화 중 오류 발생: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * 토큰화 (모든 형태소)
	 */
	public List<KoreanTokenJava> koreanTokenize(String text) {
		if (text == null || text.trim().isEmpty()) {
			log.info("텍스트 토큰화 중 오류 발생: {}", "빈 문자열");
			return Collections.emptyList();
		}

		try {
			// 텍스트 정규화
			CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
			// 토크나이즈
			Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

			return OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);
		} catch (Exception e) {
			log.error("토큰화 중 오류 발생: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * 명사만 추출
	 */
	public List<String> extractNouns(String text) {
		if (text == null || text.trim().isEmpty()) {
			log.info("텍스트 명사 추출 중 오류 발생: {}", "빈 문자열");

			return Collections.emptyList();
		}

		try {
			CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
			Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

			// 명사만 필터링
			return OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens)
				.stream()
				.filter(token -> token.getPos().toString().contains("Noun"))
				.map(KoreanTokenJava::getText)
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("명사 추출 중 오류 발생: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * 명사만 추출
	 */
	public List<String> extractAdjective(String text) {
		if (text == null || text.trim().isEmpty()) {
			log.info("텍스트 명사 추출 중 오류 발생: {}", "빈 문자열");

			return Collections.emptyList();
		}

		try {
			CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
			Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

			// 명사만 필터링
			return OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens)
				.stream()
				.filter(token -> token.getPos().toString().contains("Adjective"))
				.map(KoreanTokenJava::getText)
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("형용사 추출 중 오류 발생: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * 구문 추출 (중요 키워드)
	 */
	public List<String> extractPhrases(String text) {
		if (text == null || text.trim().isEmpty()) {
			log.info("텍스트 구문 추출 중 오류 발생: {}", "빈 문자열");

			return Collections.emptyList();
		}

		try {
			CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
			Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

			// 구문 추출 (명사와 해시태그 포함, 단일 명사도 포함)
			List<KoreanPhraseExtractor.KoreanPhrase> phrases =
				OpenKoreanTextProcessorJava.extractPhrases(tokens, true, true);

			return phrases.stream()
				.map(phrase -> phrase.text())
				.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("구문 추출 중 오류 발생: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	/**
	 * 맛집 검색용 키워드 추출 (정제된 버전)
	 */
	public Set<String> extractSearchKeywords(String text) {
		if (text == null || text.trim().isEmpty()) {
			// log.info("검색 키워드 추출중 오류 발생: {}", "빈 문자열");

			return Collections.emptySet();
		}

		try {
			// 구문과 명사를 모두 추출
			List<String> nouns = extractNouns(text);
			List<String> phrases = extractPhrases(text);

			Set<String> keywords = new HashSet<>();
			keywords.addAll(nouns);
			keywords.addAll(phrases);

			// 키워드 정제(필터링)
			return keywords.stream()
				.filter(keyword -> keyword.length() >= 2) // 키워드 길이 2 이상
				.filter(keyword -> !keyword.matches("(.)\\1{3,}")) // 반복되는 문자 제거
				.filter(keyword -> !keyword.matches("\\d+")) //  // 숫자만 제외
				.filter(keyword -> !keyword.matches("^[ㄱ-ㅎㅏ-ㅣ]+$")) // 모음/자음만 제외
				.filter(keyword -> !keyword.matches("^[^가-힣a-zA-Z0-9\\s]+$")) // 특수문자만 제외
				.filter(keyword -> !keyword.matches("\\s+"))    // 공백만 제외
				.filter(keyword -> !isStopWord(keyword))    // 불용어 제외
				.filter(this::isValidKeyword)
				.collect(Collectors.toSet());
		} catch (Exception e) {
			log.error("검색 키워드 추출 중 오류 발생: {}", e.getMessage());
			return Collections.emptySet();
		}
	}

	/**
	 * 불용어 체크 (맛집 검색에 불필요한 단어들)
	 */
	private boolean isStopWord(String word) {
		Set<String> stopWords = Set.of(
			// 조사/어미
			"이", "가", "을", "를", "에", "의", "와", "과", "도", "만", "에서", "까지",
			// 격식체
			"입니다", "습니다", "있습니다", "했습니다", "됩니다", "합니다",
			// 일반적 불용어
			"것", "거", "저", "제", "그", "뭐", "좀", "진짜", "정말", "완전"
		);
		return stopWords.contains(word);
	}

	/**
	 * 키워드 유효성 검증 (단일 통합 메서드)
	 */
	private boolean isValidKeyword(String keyword) {
		// 구문인 경우 (공백 포함)
		if (keyword.contains(" ")) {
			return isValidPhrase(keyword);
		}

		// 단일 단어인 경우
		return isValidSingleWord(keyword);
	}

	/**
	 * 구문 유효성 검증
	 */
	private boolean isValidPhrase(String phrase) {
		String[] words = phrase.split("\\s+");

		// 너무 긴 구문 제외 (4단어 이상)
		if (words.length > 4) {
			return false;
		}

		// 의미 없는 시작 패턴 제외
		if (phrase.startsWith("수 있는") ||
			phrase.startsWith("할 수 있는") ||
			phrase.startsWith("하는") ||
			phrase.startsWith("좋은")) {
			return false;
		}

		// 문어체 표현 제외
		if (phrase.contains("습니다") ||
			phrase.contains("있습니다") ||
			phrase.contains("좋겠어요") ||
			phrase.contains("적당하고")) {
			return false;
		}

		return true;
	}

	/**
	 * 단일 단어 유효성 검증
	 */
	private boolean isValidSingleWord(String word) {
		// 무의미한 단일 단어 제외
		Set<String> invalidWords = Set.of(
			"수", "있는", "할", "하는", "한", "좋은", "좋았으면",
			"적당", "함께", "가서", "먹을", "찾고"
		);

		return !invalidWords.contains(word);
	}

	// 위치 키워드 판별 로직 강화
	private boolean isLocationKeyword(String keyword) {
		Set<String> locationSuffixes = Set.of("역", "동", "구", "로", "길");
		Set<String> locationWords = Set.of("근처", "주변", "앞", "건너편", "옆");

		// 접미사 체크
		boolean hasSuffix = locationSuffixes.stream()
			.anyMatch(keyword::endsWith);

		// 위치 관련 단어 체크
		boolean hasLocationWord = locationWords.stream()
			.anyMatch(keyword::contains);

		// 지하철역 패턴 체크 (한글 + 역)
		boolean isStationPattern = keyword.matches(".*[가-힣]+역.*");

		// 행정동 패턴 체크 (한글 + 동)
		boolean isDongPattern = keyword.matches(".*[가-힣]+동.*");

		return hasSuffix || hasLocationWord || isStationPattern || isDongPattern;
	}
}
