package com.example.taste.domain.review.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 정의되지 않은 데이터는 무시한다.
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrResponseDto {
	private List<Image> images;

	public List<Image> getImages() {
		return images;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Image {
		private Receipt receipt;

		public Receipt getReceipt() {
			return receipt;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Receipt {
		private Result result;

		public Result getResult() {
			return result;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Result {
		private StoreInfo storeInfo;

		public StoreInfo getStoreInfo() {
			return storeInfo;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class StoreInfo {
		private Name name;
		private SubName subName;

		// 가게 이름
		// images/receipt/result/storeInfo/name
		public Name getName() {
			return name;
		}

		// 지점 이름
		// images/receipt/result/storeInfo/subName
		public SubName getSubName() {
			return subName;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SubName {
		private String text;

		public String getText() {
			return text;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Name {
		private String text;

		public String getText() {
			return text;
		}
	}

	public String getStoreName() {
		if (images == null || images.isEmpty())
			return null;

		Receipt receipt = images.get(0).getReceipt();
		if (receipt == null)
			return null;

		Result result = receipt.getResult();
		if (result == null)
			return null;

		StoreInfo storeInfo = result.getStoreInfo();
		if (storeInfo == null)
			return null;

		Name name = storeInfo.getName();
		if (name == null)
			return null;

		return name.getText();
	}

	public String getStoreSubName() {
		if (images == null || images.isEmpty())
			return null;

		Receipt receipt = images.get(0).getReceipt();
		if (receipt == null)
			return null;

		Result result = receipt.getResult();
		if (result == null)
			return null;

		StoreInfo storeInfo = result.getStoreInfo();
		if (storeInfo == null)
			return null;

		SubName subName = storeInfo.getSubName();
		if (subName == null)
			return null;

		return subName.getText();
	}
}
