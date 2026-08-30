package it.project.uploadContentToSocialNetworks.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/video")
public class VideoController {

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String uploadVideo(@RequestPart MultipartFile video) {
        return video.getOriginalFilename();
    }

}
