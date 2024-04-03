package com.kh.coupang.service;

import com.kh.coupang.domain.Product;
import com.kh.coupang.repo.ProductDAO;
import com.querydsl.core.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductDAO dao;

    public Page<Product> viewAll(Pageable pageable, BooleanBuilder builder){
        return dao.findAll(builder, pageable);
    } //순서 중요(builder가 먼저 와야 함)

    // 특정 카테고리의 모든 상품 조회
    public Page<Product> viewCategory(int code, Pageable pageable){
        return dao.findByCateCode(code, pageable);
    }

    public Product view(int code){
        return dao.findById(code).orElse(null);
    }

    public Product create(Product vo){
        return dao.save(vo);
    }

    public Product update(Product vo){
        if(dao.existsById(vo.getProdCode())) {
            //existsById : 존재하는지를 boolean 값으로 나타냄
            return dao.save(vo);
        }
        return null;
    }

    public Product delete(int code){
        Product target = dao.findById(code).orElse(null);
        if(target!=null){
           // dao.deleteById(code);
            dao.delete(target);
        }
      return target;
    }



}
