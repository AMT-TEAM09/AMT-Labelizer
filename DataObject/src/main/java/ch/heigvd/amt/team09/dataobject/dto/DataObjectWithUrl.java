package ch.heigvd.amt.team09.dataobject.dto;

import java.net.URL;

public record DataObjectWithUrl(String objectName, URL url, long duration) implements DataObjectResponse {
}
