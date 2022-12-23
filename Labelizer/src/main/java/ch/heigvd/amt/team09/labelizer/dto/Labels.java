package ch.heigvd.amt.team09.labelizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public record Labels(@JsonProperty("labels") Label[] _labels) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Labels labels = (Labels) o;

        return Arrays.equals(_labels, labels._labels);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_labels);
    }

    @Override
    public String toString() {
        return "Labels{" +
                "_labels=" + Arrays.toString(_labels) +
                '}';
    }
}
