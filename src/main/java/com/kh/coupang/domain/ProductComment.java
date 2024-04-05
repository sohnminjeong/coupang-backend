package com.kh.coupang.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name="product_comment")
public class ProductComment {

    @Id
    @Column(name="pro_com_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int proComCode;

    @Column(name="pro_com_desc")
    private String proComDesc;

    @Column(name="pro_com_date")
    private Date proComDate;

    @Column(name="pro_com_parent")
    private int proComParent;

    @JsonIgnore  // 가공되는 것이기 때문에 json 무시
    @ManyToOne   // 한 댓글에 부모&자식
    @JoinColumn(name="pro_com_parent", referencedColumnName = "pro_com_code", insertable = false, updatable = false)  // 조인하는 컬럼, 참조 컬럼
    private ProductComment parent;

    @ManyToOne
    @JoinColumn(name="id")
    private User user;

    @Column(name="prod_code")
    private int prodCode;
}
