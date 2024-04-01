package com.kh.coupang.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert      // 추가와 함께 primarykey가 화면에서 바로 보이게(1)
public class Product {

    @Id
    @Column(name="prod_code")   // 컬럼명과 변수명 다를 시
    @GeneratedValue(strategy = GenerationType.IDENTITY)   //추가와 함께 primarykey가 화면에서 바로 보이게(2)
    private int prodCode;   //prod_code

    @Column(name="prod_name")
    private String prodName;   //prod_name

    @Column
    private int price;


    @ManyToOne  //1:다 관계 명시 _ 카테고리 1개에 여러 상품
    @JoinColumn(name="cate_code")    // foreign key 지정
    private Category category;    // join 역할 : Category class 가져오기

}
