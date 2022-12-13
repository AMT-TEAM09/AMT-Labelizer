package ch.heigvd.amt.team09.dataobject.controller.api;

import ch.heigvd.amt.team09.dataobject.service.interfaces.DataObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataHelperController {
    private static final Logger LOG = LoggerFactory.getLogger(DataHelperController.class.getName());
    private final DataObjectService dataObjectService;

    public DataHelperController(DataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }
}
