package com.grupo13.cursos.service;

import com.grupo13.cursos.exception.AlmacenamientoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

/**
 * Almacenamiento Cloud (Amazon S3) del material de los cursos.
 * El bucket es privado; los objetos se organizan por curso.
 */
@Slf4j
@Service
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /** Sube (o reemplaza) un objeto en S3 a partir de bytes. */
    public void subirBytes(String key, byte[] contenido, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType != null ? contentType : "application/octet-stream")
                            .build(),
                    RequestBody.fromBytes(contenido));
            log.info("Objeto subido a S3: s3://{}/{}", bucket, key);
        } catch (S3Exception e) {
            throw new AlmacenamientoException("Error subiendo objeto a S3: " + key, e);
        }
    }

    /** Descarga los bytes de un objeto de S3. */
    public byte[] descargar(String key) {
        try {
            ResponseBytes<GetObjectResponse> obj = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(key).build());
            return obj.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new AlmacenamientoException("El objeto no existe en S3: " + key, e);
        } catch (S3Exception e) {
            throw new AlmacenamientoException("Error descargando objeto de S3: " + key, e);
        }
    }

    public String getBucket() { return bucket; }
}
