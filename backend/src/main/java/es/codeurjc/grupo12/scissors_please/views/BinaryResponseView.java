package es.codeurjc.grupo12.scissors_please.views;

import org.springframework.http.ResponseEntity;

public record BinaryResponseView(ResponseEntity<byte[]> responseEntity) {}
