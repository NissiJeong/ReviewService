package com.task.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3ImageUploadDummyService {

    public String upploadImageToS3(MultipartFile image) {
        String originalFilename = image.getOriginalFilename(); //원본 파일 명
        String extention = originalFilename.substring(originalFilename.lastIndexOf(".")); //확장자 명

        // dummy url 생성해서 리턴
        return "https://"+ originalFilename +".com";
    }
}
