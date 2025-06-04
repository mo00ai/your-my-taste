package com.example.taste.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
