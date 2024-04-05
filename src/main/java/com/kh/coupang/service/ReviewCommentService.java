package com.kh.coupang.service;

import com.kh.coupang.domain.QReviewComment;
import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewComment;
import com.kh.coupang.repo.ReviewCommentDAO;
import com.querydsl.core.QueryFactory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ReviewCommentService {

    @Autowired
    private ReviewCommentDAO dao;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QReviewComment qReviewComment = QReviewComment.reviewComment;

    // 댓글 추가
    public ReviewComment create(ReviewComment vo){
        return dao.save(vo);
    }

    // 리뷰 1개당 댓글 조회 --> 안써용
    public List<ReviewComment> findByReviewCode(int code){
        return dao.findByProdCode(code);
    }

    // 상위 댓글 조회
    public List<ReviewComment> getTopLevelCommenst(int code){
        return queryFactory
                .selectFrom(qReviewComment)
                .where(qReviewComment.reviComParent.eq(0))
                .where(qReviewComment.reviCode.eq(code))
                .orderBy(qReviewComment.reviComDate.desc())
                .fetch();
    }


    // 하위 댓글 조회
    public List<ReviewComment> getRepliesComments(int parent, int code){
        return queryFactory
                .selectFrom(qReviewComment)
                .where(qReviewComment.reviComParent.eq(parent))
                .where(qReviewComment.reviCode.eq(code))
                .orderBy(qReviewComment.reviComDate.asc())
                .fetch();
    }
}
