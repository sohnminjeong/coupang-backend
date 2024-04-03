package com.kh.coupang.service;

import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewImage;
import com.kh.coupang.repo.ReviewDAO;
import com.kh.coupang.repo.ReviewImageDAO;
import com.querydsl.core.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewDAO review;

    @Autowired
    private ReviewImageDAO image;

    public Review create(Review vo){
        return review.save(vo);
    }

    // image가 여러개 들어갈 수 있기 때문에 메서드를 Review와 따로 진행하는 것이 수월
    public ReviewImage createImg(ReviewImage vo){
        return image.save(vo);
    }

    public Page<Review> viewAll(Pageable pageable, BooleanBuilder builder){
        return review.findAll(builder, pageable);
    }

}
