package com.kh.coupang.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert   // 추가 시 auto가 바로 보이게(1)
@Table(name="review_image") // 테이블에 '_'(언더바)가 들어간 경우에 사용
public class ReviewImage {

    @Id
    @Column(name="revi_img_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 추가 시 auto가 바로 보이게(2)
    private int reviImgCode;

    @ManyToOne
    @JoinColumn(name="revi_code")
    @JsonIgnore
    private Review reviCode;

    @Column(name="revi_url")
    private String reviUrl;

}
