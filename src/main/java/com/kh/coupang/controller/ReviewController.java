package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.ReviewService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @PostMapping("/review")
    public ResponseEntity<Review> create(ReviewDTO dto) throws IOException {

        // review부터 추가하여 revi_code가 담긴 review!
        // Review vo 값들 담아서 요청
        Review vo = new Review();

        vo.setId(dto.getId());
        vo.setProdCode(dto.getProdCode());
        vo.setReviTitle(dto.getReviTitle());
        vo.setReviDesc(dto.getReviDesc());
        vo.setRating(dto.getRating());

        Review result = service.create(vo);

        // review_image에는 revi_code가 필요!
        for(MultipartFile file : dto.getFiles()){

            ReviewImage imgVo = new ReviewImage();
            log.info("fileName : "+file.getOriginalFilename());

            String fileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();

            String saveName = uploadPath + File.separator + "review" + File.separator + uuid + "_" + fileName;
            log.info("saveName : " + saveName);
            Path savePath = Paths.get(saveName);
            log.info("savePath : " + savePath);
            file.transferTo(savePath);

            imgVo.setReviUrl(saveName);
            imgVo.setReviCode(result);

            service.createImg(imgVo);

            log.info("voImg.getReviUrl : " +  imgVo.getReviUrl());
        }

        return result!=null ?
                ResponseEntity.status(HttpStatus.CREATED).body(result):
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // jpa방식 _ 정렬 : sort(id만 가능하고 다른건 불가_ex:product category)
    /*
    @GetMapping("/review")
    public ResponseEntity<List<Review>> viewAll(@RequestParam(name="page", defaultValue = "1") int page){
        // 정렬하고 싶으면 sort 사용
        Sort sort = Sort.by("reviCode").descending();  // 거꾸로 정렬

        Pageable pageable = PageRequest.of(page-1, 10, sort);
        Page<Review> list = service.viewAll(pageable);

        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
    }
    */
    // querydsl 방식 : prodCode에 따라 내림차순&1페이지에 10개씩 페이징 처리
    @GetMapping("/review")
    public ResponseEntity<List<Review>> viewAll(@RequestParam(name="prodCode", required = false) Integer prodCode, @RequestParam(name="page", defaultValue = "1") int page){
        // 정렬하고 싶으면 sort 사용
        Sort sort = Sort.by("reviCode").descending();  // 거꾸로 정렬
        Pageable pageable = PageRequest.of(page-1, 10, sort);

        QReview qReview = QReview.review;
        BooleanBuilder builder = new BooleanBuilder();

        if(prodCode!=null){
            BooleanExpression expression = qReview.prodCode.eq(prodCode);

            builder.and(expression);
        }

        Page<Review> list = service.viewAll(pageable, builder);

        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
    }

}
