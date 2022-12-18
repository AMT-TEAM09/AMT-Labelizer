package ch.heigvd.amt.team09.dataobject.dto;

import java.net.URL;
import java.time.Duration;

public record ObjectModel(String objectName, URL url, Duration urlDuration) {
}
