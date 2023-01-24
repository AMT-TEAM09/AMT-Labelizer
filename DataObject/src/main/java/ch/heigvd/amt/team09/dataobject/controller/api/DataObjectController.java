package ch.heigvd.amt.team09.dataobject.controller.api;

import ch.heigvd.amt.team09.dataobject.assembler.ResponseModelAssembler;
import ch.heigvd.amt.team09.dataobject.dto.DataObject;
import ch.heigvd.amt.team09.dataobject.dto.DataObjectResponseModel;
import ch.heigvd.amt.team09.dataobject.dto.DataObjectWithUrl;
import ch.heigvd.amt.team09.dataobject.exception.DeleteFailedException;
import ch.heigvd.amt.team09.dataobject.exception.FileUploadException;
import ch.heigvd.amt.team09.dataobject.exception.ObjectAlreadyExistsException;
import ch.heigvd.amt.team09.dataobject.exception.ObjectNotFoundException;
import ch.heigvd.amt.team09.dataobject.service.interfaces.DataObjectService;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@RestController
@Validated
public class DataObjectController {
    private static final Logger LOG = LoggerFactory.getLogger(DataObjectController.class.getName());
    private final DataObjectService dataObjectService;
    private final ResponseModelAssembler assembler;

    public DataObjectController(DataObjectService dataObjectService, ResponseModelAssembler assembler) {
        this.dataObjectService = dataObjectService;
        this.assembler = assembler;
    }

    // pas pénalisé mais mettez le /data-object/v1 sur le controller avec
    // @RequestMapping pour éviter de le répéter, ça améliore la lisibilité
    @DeleteMapping(value = "data-object/v1/objects")
    public ResponseEntity<Object> delete(@RequestParam Optional<Boolean> recursive) {
        if (!dataObjectService.exists()) {
            throw new ObjectNotFoundException();
        }

        try {
            if (recursive.isPresent()) {
                dataObjectService.delete(recursive.get());
            } else {
                dataObjectService.delete();
            }

            return ResponseEntity.noContent().build();
        } catch (DataObjectService.ObjectNotEmptyException e) {
            LOG.info("Failed to delete root object: {}", e.getMessage());
            throw new DeleteFailedException("Root object is not empty");
        } catch (DataObjectService.ObjectNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @PostMapping(value = "data-object/v1/objects", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public DataObjectResponseModel upload(@RequestPart String objectName,
                                          @RequestPart MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            LOG.error("Failed to read file: {}", e.getMessage());
            throw new FileUploadException("File %s could not be read".formatted(file.getOriginalFilename()));
        }

        if (dataObjectService.exists(objectName)) {
            throw new ObjectAlreadyExistsException(objectName);
        }

        try {
            dataObjectService.create(objectName, content);
            return assembler.toModel(new DataObject(objectName))
                    .withSelf(ResponseModelAssembler.LINK_UPLOAD);
        } catch (DataObjectService.ObjectAlreadyExistsException e) {
            throw new IllegalStateException(e);
        }
    }

    @DeleteMapping(value = "data-object/v1/objects", params = "objectName")
    public ResponseEntity<Object> delete(@RequestParam String objectName, @RequestParam Optional<Boolean> recursive) {
        if (!dataObjectService.exists(objectName)) {
            throw new ObjectNotFoundException(objectName);
        }

        try {
            if (recursive.isPresent()) {
                dataObjectService.delete(objectName, recursive.get());
            } else {
                dataObjectService.delete(objectName);
            }

            return ResponseEntity.noContent().build();
        } catch (DataObjectService.ObjectNotEmptyException e) {
            throw new DeleteFailedException(e.getMessage());
        } catch (DataObjectService.ObjectNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @GetMapping("data-object/v1/objects")
    public DataObjectResponseModel publish(@RequestParam String objectName,
                                           @RequestParam Optional<@Positive Long> duration) {
        if (!dataObjectService.exists(objectName)) {
            throw new ObjectNotFoundException(objectName);
        }

        try {
            var url = duration.isPresent()
                    ? dataObjectService.publish(objectName, Duration.ofSeconds(duration.get()))
                    : dataObjectService.publish(objectName);

            return assembler.toModel(
                    new DataObjectWithUrl(
                            objectName,
                            url,
                            duration.orElse(dataObjectService.getDefaultUrlExpirationTime().toSeconds())
                    )
            ).withSelf(ResponseModelAssembler.LINK_PUBLISH);
        } catch (DataObjectService.ObjectNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
