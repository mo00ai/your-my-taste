package com.example.taste.domain.image.entity;

import com.example.taste.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "image")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String url;

	@Column(nullable = false)
	private String originFileName;

	@Column(nullable = false)
	private String uploadFileName;

	@Column(nullable = false)
	private Long fileSize;

	@Column(nullable = false, length = 50)
	private String fileExtension;

	@Builder
	public Image(String url, String originFileName, String uploadFileName, Long fileSize, String fileExtension) {
		this.url = url;
		this.originFileName = originFileName;
		this.uploadFileName = uploadFileName;
		this.fileSize = fileSize;
		this.fileExtension = fileExtension;
	}
}
