package com.task.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    public void isValidReview() throws Exception {
        ReviewRequestDto review = new ReviewRequestDto(1L, 7, "content", null);

        String reviewJsonString = objectMapper.writeValueAsString(review);

        System.out.println("reviewJsonString = " + reviewJsonString);

        MockMultipartFile file1 = new MockMultipartFile("image", "empty.txt", "text/plain", "".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("requestDto", "jsondata", "application/json", reviewJsonString.getBytes(StandardCharsets.UTF_8));

        mvc.perform(multipart("/products/1/reviews")
                    .file(file1)
                    .file(file2)
                    .accept(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}