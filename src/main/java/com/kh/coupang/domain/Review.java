package com.kh.coupang.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert  // 추가와 함께 auto키가 보이도록(1)
public class Review {

    @Id
    @Column(name="revi_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 추가와 함께 auto키가 보이도록(1)
    private int reviCode;

    @Column
    private String id;

    @Column(name="prod_code")
    private int prodCode;

    @Column(name="revi_title")
    private String reviTitle;

    @Column(name="revi_desc")
    private String reviDesc;

    @Column(name="revi_date")
    private Date reviDate;

    @Column
    private int rating;

    @OneToMany(mappedBy="reviCode")  // 리뷰테이블이랑 연결 (이미지들)
    // ReviewImage에서 reviCode에 ManyToOne에 적어놓은 컬럼명과 일치해야 함
    private List<ReviewImage> images;


}
