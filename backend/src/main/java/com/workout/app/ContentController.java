package com.workout.app;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class ContentController {
//
//    private final PostService postService; //Service 불러오기
//
//    @GetMapping
//    public ResponseEntity<List<PostResponseDto>> getPosts(@RequestParam(required = false) String keyword)
}
