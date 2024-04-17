package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.ReviewCommentService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins = {"*"}, maxAge = 6000)
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Autowired
    private ReviewCommentService comment;

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

    // 상품 전체 보기를 상품 1개에 있는 리뷰 전체 보기로 코드 변경함
    // 상품 1개에 있는 리뷰 전체 보기
    // http://localhost:8080/api/public/product/2/review
    // /public/product/{code}/review
    @GetMapping("/public/product/{code}/review")
    public ResponseEntity<List<Review>> viewAll(@RequestParam(name="page", defaultValue = "1") int page, @PathVariable(name="code")int code){
        // @RequestParam(name="prodCode", required = false) Integer prodCode,
        // 정렬하고 싶으면 sort 사용
        Sort sort = Sort.by("reviCode").descending();  // 거꾸로 정렬
        Pageable pageable = PageRequest.of(page-1, 10, sort);

        QReview qReview = QReview.review;
        BooleanBuilder builder = new BooleanBuilder();

        BooleanExpression expression = qReview.prodCode.eq(code);
        builder.and(expression);

//        if(prodCode!=null){
//            BooleanExpression expression = qReview.prodCode.eq(prodCode);
//
//            builder.and(expression);
//        }

//        Page<Review> list = service.viewAll(pageable, builder);

//        return ResponseEntity.status(HttpStatus.OK).body(review.veiwQll);
        return ResponseEntity.status(HttpStatus.OK).body(service.viewAll(pageable, builder).getContent());
    }

    // 리뷰 1개 조회
    @GetMapping("/review/{code}")
    public ResponseEntity<Review> view(@PathVariable("code") int code){
        Review vo = service.view(code);
        return ResponseEntity.status(HttpStatus.OK).body(vo);
    }

    public Object authentication(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return authentication.getPrincipal();
    }

    // 리뷰 댓글 추가
    @PostMapping("/review/comment")
    public ResponseEntity createComment(@RequestBody ReviewComment vo){

       // SecurityContext securityContext = SecurityContextHolder.getContext();
       // Authentication authentication = securityContext.getAuthentication();
       // Object principal = authentication.getPrincipal();
        Object principal = authentication();

        if(principal instanceof User){
            User user = (User) principal;
            vo.setUser(user);
            return ResponseEntity.ok().body(comment.create(vo));
        }
        log.info("vo : " + vo);
        return ResponseEntity.badRequest().build();
    }

    // 상품 1개에 따른 리뷰 조회 - (상하위 댓글 분류 전)
//    @GetMapping("/review/{code}/comment")
//    public ResponseEntity<List<ReviewComment>> viewComment(@PathVariable(name="code") int code){
//        return ResponseEntity.ok(comment.findByReviewCode(code));
//    }

    @GetMapping("/public/review/{code}/comment")
    public ResponseEntity<List<ReviewCommentDTO>> viewComment(@PathVariable(name="code") int code){
        List<ReviewComment> topList = comment.getTopLevelCommenst(code);
        List<ReviewCommentDTO> response = new ArrayList<>();

        for(ReviewComment top : topList){

            // 하위 댓글 처리 -> 반복 돌아야 함
            List<ReviewComment> replies = comment.getRepliesComments(top.getReviComCode(), code);
            List<ReviewCommentDTO> repliesDTO = new ArrayList<>();

            for(ReviewComment reply : replies){
                ReviewCommentDTO dto = ReviewCommentDTO.builder()
                        .reviComCode(reply.getReviComCode())
                        .reviCode(reply.getReviCode())
                        .reviComDesc(reply.getReviComDesc())
                        .reviComDate(reply.getReviComDate())
                        .user(UserDTO.builder()
                                .id(reply.getUser().getId())
                                .name(reply.getUser().getName())
                                .build())
                        .build();
                repliesDTO.add(dto);
            }

            // 상위 처리
            ReviewCommentDTO dto = ReviewCommentDTO.builder()
                    .reviCode(top.getReviCode())
                    .reviComCode(top.getReviComCode())
                    .reviComDesc(top.getReviComDesc())
                    .reviComDate(top.getReviComDate())
                    .user(UserDTO.builder()
                            .id(top.getUser().getId())
                            .name(top.getUser().getName())
                            .build())
                    .replies(repliesDTO)
                    .build();
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }
}
