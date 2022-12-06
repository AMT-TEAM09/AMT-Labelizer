package ch.heigvd.amt.team09.labelizer.controller.api;

import ch.heigvd.amt.team09.labelizer.assembler.LabelsModelAssembler;
import ch.heigvd.amt.team09.labelizer.dto.LabelsModel;
import ch.heigvd.amt.team09.labelizer.exception.InvalidBase64Exception;
import ch.heigvd.amt.team09.labelizer.exception.UnknownException;
import ch.heigvd.amt.team09.labelizer.exception.UnreachableUrlException;
import ch.heigvd.amt.team09.labelizer.service.RekognitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Optional;

@RestController
public class RekognitionController {
    private static final Logger LOG = LoggerFactory.getLogger(RekognitionController.class.getName());
    private final RekognitionService rekognitionService;
    private final LabelsModelAssembler assembler;

    public RekognitionController(RekognitionService rekognitionService, LabelsModelAssembler assembler) {
        this.rekognitionService = rekognitionService;
        this.assembler = assembler;
    }

    private static boolean isUrlValid(String url) {
        try {
            var huc = (HttpURLConnection) new URL(url).openConnection();
            var responseCode = huc.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            LOG.error("Requested URL '{}' is not reachable", url, e);
            return false;
        }
    }

    private static boolean isBase64Valid(String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            LOG.error("Requested base64 '{}' is not valid", base64, e);
            return false;
        }
    }

    @GetMapping("/labelize/url/{url}?minConfidence={minConfidence}&maxLabels={maxLabels}")
    public LabelsModel fromUrl(@PathVariable String url,
                               @PathVariable Optional<Float> minConfidence,
                               @PathVariable Optional<Integer> maxLabels) {
        if (!isUrlValid(url)) {
            throw new UnreachableUrlException(url);
        }

        try {
            return assembler.toModel(
                    rekognitionService.execute(url, builder -> {
                        minConfidence.ifPresent(builder::minConfidence);
                        maxLabels.ifPresent(builder::maxLabels);
                    })
            );
        } catch (IOException e) {
            LOG.error("An error occurred while analyzing {}", url, e);
            throw new UnknownException();
        }
    }

    @GetMapping("/labelize/base64/{base64}?minConfidence={minConfidence}&maxLabels={maxLabels}")
    public LabelsModel fromBase64(@PathVariable String base64,
                                  @PathVariable Optional<Float> minConfidence,
                                  @PathVariable Optional<Integer> maxLabels) {
        if (!isBase64Valid(base64)) {
            throw new InvalidBase64Exception(base64);
        }

        return assembler.toModel(
                rekognitionService.executeFromBase64(base64, builder -> {
                    minConfidence.ifPresent(builder::minConfidence);
                    maxLabels.ifPresent(builder::maxLabels);
                })
        );
    }
}
