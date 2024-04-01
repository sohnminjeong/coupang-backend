package com.kh.coupang.controller;

import com.kh.coupang.domain.Product;
import com.kh.coupang.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/*")
public class ProductController {

    // 메서드명은 Service랑 동일!
    @Autowired
    private ProductService service;   // 보통 service를 많이 사용하게 되는 경우 service->product로 이름 변경

    @GetMapping("/product")
    public ResponseEntity<List<Product>> viewAll(@RequestParam(name="category", required = false) Integer category) {
        log.info("category: " + category);
        List<Product> list = service.viewAll();
        return category==null ?
                ResponseEntity.status(HttpStatus.OK).body(list) :
                ResponseEntity.status(HttpStatus.OK).body(service.viewCategory(category));
    }

    @GetMapping("/product/{code}")
    public ResponseEntity<Product> view(@PathVariable("code") int code) {
        Product vo = service.view(code);
        return ResponseEntity.status(HttpStatus.OK).body(vo);
    }

    @PostMapping("/product")
    public ResponseEntity create(@RequestBody Product vo) {
        Product result = service.create(vo);
        //return ResponseEntity.status(HttpStatus.OK).build(); // OK 말고 CREATED도 가능
        if (result != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        // 잘못된 요청일 때 : BAD_REQUEST하고 요청값 없기 때문에 body가 아닌 build
    }

    @PutMapping("/product")
    public ResponseEntity<Product> update(@RequestBody Product vo) {
        Product result = service.update(vo);
        // 삼항연산자 사용
        return (result != null) ?
                ResponseEntity.status(HttpStatus.ACCEPTED).body(result)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        //return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/product/{code}")
    public ResponseEntity<Product> delete(@PathVariable(name="code") int code) {
        Product result = service.delete(code);
        //return ResponseEntity.status(HttpStatus.OK).build();
        return (result != null) ?
                ResponseEntity.status(HttpStatus.ACCEPTED).body(result)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}