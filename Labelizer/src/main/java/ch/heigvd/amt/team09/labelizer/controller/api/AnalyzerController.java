package ch.heigvd.amt.team09.labelizer.controller.api;

import ch.heigvd.amt.team09.labelizer.assembler.LabelsModelAssembler;
import ch.heigvd.amt.team09.labelizer.controller.request.AnalyzerRequest;
import ch.heigvd.amt.team09.labelizer.dto.LabelsModel;
import ch.heigvd.amt.team09.labelizer.exception.InvalidBase64Exception;
import ch.heigvd.amt.team09.labelizer.exception.UnreachableUrlException;
import ch.heigvd.amt.team09.labelizer.service.interfaces.AnalyzerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

@RestController
public class AnalyzerController {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyzerController.class.getName());
    private final AnalyzerService rekognitionService;
    private final LabelsModelAssembler assembler;

    public AnalyzerController(AnalyzerService analyzer, LabelsModelAssembler assembler) {
        this.rekognitionService = analyzer;
        this.assembler = assembler;
    }

    private static boolean isUrlValid(String url) {
        try {
            var huc = (HttpURLConnection) new URL(url).openConnection();
            var responseCode = huc.getResponseCode();
            return responseCode == HttpStatus.OK.value();
        } catch (IOException e) {
            LOG.info("Requested URL is not reachable: {}", e.getMessage());
            return false;
        }
    }

    private static boolean isBase64Valid(String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            LOG.info("Requested base64 is not valid: {}", e.getMessage());
            return false;
        }
    }

    @PostMapping("analyzer/v1/url")
    public LabelsModel fromUrl(@Valid @RequestBody AnalyzerRequest request) {
        var url = request.source();
        if (!isUrlValid(url)) {
            throw new UnreachableUrlException(url);
        }

        try {
            return assembler.toModel(
                    rekognitionService.execute(url, builder -> {
                        request.minConfidence().ifPresent(builder::minConfidence);
                        request.maxLabels().ifPresent(builder::maxLabels);
                    })
            ).withSelf(LabelsModelAssembler.LINK_URL);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @PostMapping("analyzer/v1/base64")
    public LabelsModel fromBase64(@Valid @RequestBody AnalyzerRequest request) {
        var base64 = request.source();

        if (!isBase64Valid(base64)) {
            throw new InvalidBase64Exception(base64);
        }

        return assembler.toModel(
                rekognitionService.executeFromBase64(base64, builder -> {
                    request.minConfidence().ifPresent(builder::minConfidence);
                    request.maxLabels().ifPresent(builder::maxLabels);
                })
        ).withSelf(LabelsModelAssembler.LINK_BASE64);
    }
}
