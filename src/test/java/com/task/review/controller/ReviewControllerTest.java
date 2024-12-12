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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        ReviewRequestDto review = new ReviewRequestDto(1L, 5, "content", null);

        String reviewJsonString = objectMapper.writeValueAsString(review);

        mvc.perform(post("/products/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewJsonString))
                .andExpect(status().isBadRequest());
    }
}