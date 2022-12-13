package ch.heigvd.amt.team09.dataobject.controller.request;

import org.springframework.web.multipart.MultipartFile;

public record UploadRequest(MultipartFile file) {

}
