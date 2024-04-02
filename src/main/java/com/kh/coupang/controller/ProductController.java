package com.kh.coupang.controller;

import com.kh.coupang.domain.Category;
import com.kh.coupang.domain.Product;
import com.kh.coupang.domain.ProductDTO;
import com.kh.coupang.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
public class ProductController {

    // 메서드명은 Service랑 동일!
    @Autowired
    private ProductService service;   // 보통 service를 많이 사용하게 되는 경우 service->product로 이름 변경

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;  // D:\\upload

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
    public ResponseEntity create(ProductDTO dto) throws IOException {  //@RequstBody는 form-data 방식이 안받아짐
       /* - 매개변수가 @RequestBody Product vo일 경우
       Product result = service.create(vo);
        //return ResponseEntity.status(HttpStatus.OK).build(); // OK 말고 CREATED도 가능
        if (result != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }*/
        // log.info() : System.out || consol.log 같은 기능(@Slf4j가 있어야 사용 가능)
        log.info("dto : " + dto);
        log.info("file : " + dto.getFile());

        String fileName = dto.getFile().getOriginalFilename();  // file이름 확인
        log.info("fileName : " + fileName);

        // UUID (같은 파일명때문에 랜덤값 지정)
        String uuid = UUID.randomUUID().toString();

        // 실제 저장될 이름
        String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;

        // 파일 업로드
        Path savePath = Paths.get(saveName);
        dto.getFile().transferTo(savePath);  // 파일 업로드 실제로 일어나고 있음!

        // Product vo 값들 담아서 요청!
        Product vo = new Product();
        vo.setProdName(dto.getProdName());
        vo.setPrice(dto.getPrice());
        vo.setProdPhoto(saveName);   // 경로+파일명이 들어가야 함

        Category category = new Category();
        category.setCateCode(dto.getCateCode());
        vo.setCategory(category);

        Product result = service.create(vo);
        if(result!=null){
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        // 잘못된 요청일 때 : BAD_REQUEST하고 요청값 없기 때문에 body가 아닌 build
    }

    @PutMapping("/product")
    public ResponseEntity<Product> update(ProductDTO dto) throws IOException { // @RequestBody Product vo

        Product vo = new Product();
        vo.setProdCode(dto.getProdCode());
        vo.setPrice(dto.getPrice());
        vo.setProdName(dto.getProdName());

        Category category = new Category();
        category.setCateCode(dto.getCateCode());
        vo.setCategory(category);

        Product prev = service.view(dto.getProdCode());

        log.info("file is Empty : " + dto.getFile().isEmpty());  // 사진 없으면 true, 있으면 false
        // if : 새로운 사진 없는 경우 / else if : 새로운 사진 있는 경우
        if(dto.getFile().isEmpty()){
            // 만약 새로운 사진이 없는 경우 -> 기존 사진 경로 그대로 vo로 담아내야 한다!
            //Product prev = service.view(dto.getProdCode());
            //vo.setProdPhoto(prev.getProdPhoto());
            if(prev.getProdPhoto()!=null){
                // 기존 사진은 있고 새로운 사진은 없는 경우
                vo.setProdPhoto(prev.getProdPhoto());
            } else{
                // 기존 사진도 없고 새로운 사진도 없는 경우
            }

        } else if(prev.getProdPhoto() == null && !dto.getFile().isEmpty()) {

            // 기존 사진은 없지만 새로운 사진은 있는 경우

            // 기존 사진도 있고 새로운 사진도 있는 경우

            // 사진이 처음에 없을 때 수정으로 사진 삽입
            String fileName = dto.getFile().getOriginalFilename();  // file이름 확인
            String uuid = UUID.randomUUID().toString();
            String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
            Path savePath = Paths.get(saveName);
            dto.getFile().transferTo(savePath);  // 파일 업로드 실제로 일어나고 있음!

            vo.setProdPhoto(saveName);
        } else {
            //
            // 기존 데이터를 가져와야 하는 상황!
            log.info("prev.getProdPhoto() : " +  prev.getProdPhoto());
            // 기존 사진은 삭제하고, 새로운 사진을 추가
            // 1) 기존 사진 삭제
            File file = new File(prev.getProdPhoto());
            file.delete();
            // 2) 새로운 사진 추가
            String fileName = dto.getFile().getOriginalFilename();  // file이름 확인
            String uuid = UUID.randomUUID().toString();
            String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
            Path savePath = Paths.get(saveName);
            dto.getFile().transferTo(savePath);  // 파일 업로드 실제로 일어나고 있음!

            vo.setProdPhoto(saveName);
        }

        // 매개변수가 @RequestBody Product vo 일 때는 위에 문장 없이 밑에만 진행
        Product result = service.update(vo);
        // 삼항연산자 사용
        return (result != null) ?
                ResponseEntity.status(HttpStatus.ACCEPTED).body(result)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        //return ResponseEntity.status(HttpStatus.OK).build();

         
    }

    @DeleteMapping("/product/{code}")
    public ResponseEntity<Product> delete(@PathVariable(name="code") int code) {
        // 파일 삭제 로직
        // 조건 추가 : getProdPhto == null 경우
        Product prev = service.view(code);
        File file = new File(prev.getProdPhoto());
        file.delete();


        Product result = service.delete(code);
        //return ResponseEntity.status(HttpStatus.OK).build();
        return (result != null) ?
                ResponseEntity.status(HttpStatus.ACCEPTED).body(result)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}