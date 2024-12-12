package com.task.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.review.dto.ProductResponseDto;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.service.ReviewService;
import com.task.review.service.ReviewServiceTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    public void getReviews() throws Exception {
        List<ReviewResponseDto> list = List.of(new ReviewResponseDto(1L, 4, "test", null));
        ProductResponseDto productResponseDto = new ProductResponseDto(1L, 4, 0L, list);

        Mockito.when(reviewService.getReviews(1L, 0L, 10)).thenReturn(productResponseDto);

        mvc.perform(get("/products/1/reviews?cursor=0&size=10").contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("reviews[0].userId").value(1L));
    }
}