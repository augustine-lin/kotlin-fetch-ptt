package com.example.models

import io.minio.*
import io.minio.errors.MinioException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*


object FileUploader {
    @JvmStatic
    private lateinit var minioClient: MinioClient

    init {
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key.
            minioClient = MinioClient.builder()
                .endpoint("http://10.99.253.131:9000")
                .credentials("minio", "minio123")
                .build()

            // Make 'test1' bucket if not exist.
            val found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("test1").build())
            if (!found) {
                // Make a new bucket called 'test1'.
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("test1").build())
            } else {
                println("Bucket 'test1' already exists.")
            }
        } catch (e: Exception) {
            println("Error occurred: $e")
        }
    }

    fun upload(img: ByteArray) {
        try {

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket("test1")
                    .`object`(UUID.randomUUID().toString())
                    .stream(ByteArrayInputStream(img), -1, 5242880)
                    .contentType("image/jpeg")
                    .build()
            )
        } catch (e: MinioException) {
            println("Error occurred: $e")
            println("HTTP trace: " + e.httpTrace())
        }
    }
}
