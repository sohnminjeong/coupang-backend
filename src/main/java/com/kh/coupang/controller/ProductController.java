package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.CategoryService;
import com.kh.coupang.service.ProductCommentService;
import com.kh.coupang.service.ProductService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
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
import org.springframework.web.bind.annotation.*;

import javax.swing.*;

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
public class ProductController {

    // 메서드명은 Service랑 동일!
    @Autowired
    private ProductService service;   // 보통 service를 많이 사용하게 되는 경우 service->product로 이름 변경

    @Autowired
    private ProductCommentService comment;

    @Autowired
    private CategoryService category;


    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;  // D:\\upload

    // 카테고리 가져오기
    @GetMapping("/public/category")
    public ResponseEntity<List<CategoryDTO>> categoryView(){
        List<Category> topList = category.topCategory();
        List<CategoryDTO> response = new ArrayList<>();

        for(Category item : topList){
            CategoryDTO dto = CategoryDTO.builder()
                    .cateIcon(item.getCateIcon())
                    .cateName(item.getCateName())
                    .cateCode(item.getCateCode())
                    .cateUrl(item.getCateUrl())
                    .subCategories(category.bottomCategory(item.getCateCode()))
                    .build();
            response.add(dto);
        }

        return ResponseEntity.ok(response);
    }


    // jpa 방식
    /*
    @GetMapping("/product")
    public ResponseEntity<List<Product>> viewAll(@RequestParam(name="category", required = false) Integer category, @RequestParam(name="page", defaultValue = "1") int page) {
        //log.info("category: " + category);
        //log.info("page : " + page);
        Sort sort = Sort.by("prodCode").descending();  // 거꾸로 정렬(큰->작은)
        Pageable pageable = PageRequest.of(page-1, 10, sort);
        // page의 default값이 1이기 때문에 0으로 시작해야해서 -1
        Page<Product> list = service.viewAll(pageable);
        return category==null ?
                ResponseEntity.status(HttpStatus.OK).body(list.getContent()) :
                ResponseEntity.status(HttpStatus.OK).body(service.viewCategory(category, pageable).getContent());
    }
    */
    // querydsl 방식
    @GetMapping("/public/product")
    public ResponseEntity<List<Product>> viewAll(@RequestParam(name="category", required = false) Integer category, @RequestParam(name="page", defaultValue = "1") int page) {
        //log.info("category: " + category);
        //log.info("page : " + page);
        Sort sort = Sort.by("prodCode").descending();  // 거꾸로 정렬(큰->작은)
        Pageable pageable = PageRequest.of(page-1, 10, sort);

        // QueryDSL
        // 1. 가장 먼저 동적 처리하기 위한 Q도메인 클래스 얻어오기
        // Q도메인 클래스를 이용하면 Entity 클래스에 선언된 필드들을 변수로 활용할 수 있음
        QProduct qProduct = QProduct.product;

        // 2. BooleanBuilder : where 문에 들어가는 조건들을 넣어주는 컨테이너
        BooleanBuilder builder = new BooleanBuilder();

        if(category!=null){
            // 3. 원하는 조건은 필드값과 같이 결합해서 생성
            BooleanExpression expression = qProduct.category.cateCode.eq(category);

            // 4. 만들어진 조건은 where문에 and 나 or 같은 키워드와 결합
            builder.and(expression);
            // builder.or도 존재
        }

        // 5. BooleanBuilder는 QuerydslPredicateExcutor 인터페이스의 findAll() 사용(ProductDAO에 작성)
        Page<Product> list = service.viewAll(pageable, builder);

        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
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

    // 상품 1개 조회
    @GetMapping("public/product/{code}")
    public ResponseEntity<Product> view(@PathVariable("code") int code) {
        Product vo = service.view(code);
        return ResponseEntity.status(HttpStatus.OK).body(vo);
    }

    // 상품 댓글 추가
    @PostMapping("/product/comment")
    public ResponseEntity createComment(@RequestBody ProductComment vo){

        // 시큐리티에 담은 로그인한 사용자의 정보 가져오기
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        Object principal = authentication.getPrincipal();
        log.info("principal : " + principal);
        if(principal instanceof User){
            User user = (User) principal;
            vo.setUser(user);
            return ResponseEntity.ok().body(comment.create(vo));
        }
        log.info("vo : " + vo);
        return ResponseEntity.badRequest().build();
    }

    // 상품 1개에 따른 댓글 조회(분류전)
//    @GetMapping("/product/{code}/comment")
//    public ResponseEntity<List<ProductComment>> viewComment(@PathVariable(name="code") int code){
//        return ResponseEntity.ok(comment.findByProdCode(code));
//    }

    // 상품 1개에 따른 댓글 조회(상위&하위 분류후) -> 전체 다 보여줘야 하는 상황!
    @GetMapping("/public/product/{code}/comment")
    public ResponseEntity<List<ProductCommentDTO>> viewComment(@PathVariable(name="code") int code){
        List<ProductComment> topList = comment.getTopLevelComments(code);
        List<ProductCommentDTO> response = commentDetailtList(topList, code);
                /*new ArrayList<>();

      //  for(ProductComment top : topList){
            // 하위 댓글 처리 부분
            List<ProductComment> replies = comment.getRepliesComments(top.getProComCode(), code); // 하위 댓글들
           // List<ProductCommentDTO> repliesDTO = new ArrayList<>();

           // for(ProductComment reply : replies){
                /*
                ProductCommentDTO dto = ProductCommentDTO.builder()
                        .prodCode(reply.getProdCode())
                        .proComCode(reply.getProComCode())
                        .proComDesc(reply.getProComDesc())
                        .proComDate(reply.getProComDate())
                        .user(UserDTO.builder()
                                .id(reply.getUser().getId())
                                .name(reply.getUser().getName())
                                .build())
                        .build();

              //  ProductCommentDTO dto = comment(reply);
                repliesDTO.add(dto);
           // }

            // 상위 댓글 처리 부분
            /*
            ProductCommentDTO dto = ProductCommentDTO.builder()
                    .prodCode(top.getProdCode())
                    .proComCode(top.getProComCode())
                    .proComDesc(top.getProComDesc())
                    .proComDate(top.getProComDate())
                    .user(UserDTO.builder()
                            .id(top.getUser().getId())
                            .name(top.getUser().getName())
                            .build())
                    .replies(repliesDTO) // 하위댓글 관련
                    .build();

          //  ProductCommentDTO dto = comment(top);
            dto.setReplies(repliesDTO);
            response.add(dto);
        }
*/
        return ResponseEntity.ok(response);
    }

    // 나머지 공통 빼기
    public List<ProductCommentDTO> commentDetailtList(List<ProductComment> comments, int code){
        List<ProductCommentDTO> response = new ArrayList<>();

        for(ProductComment item : comments){
            List<ProductComment> replies = comment.getRepliesComments(item.getProComCode(), code);
            List<ProductCommentDTO> repliesDTO = commentDetailtList(replies, code);
            ProductCommentDTO dto = commentDetail(item);
            dto.setReplies(repliesDTO);
            response.add(dto);
        }
        return response;
    }

    // 공통 부분 빼기 : .builder().build()
    public ProductCommentDTO commentDetail(ProductComment vo){
        return ProductCommentDTO.builder()
                .prodCode(vo.getProdCode())
                .proComCode(vo.getProComCode())
                .proComDesc(vo.getProComDesc())
                .proComDate(vo.getProComDate())
                .user(UserDTO.builder()
                        .id(vo.getUser().getId())
                        .name(vo.getUser().getName())
                        .build())
                .build();
    }
}