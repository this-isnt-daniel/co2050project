package com.forensicdept.document.service;

import java.io.InputStream;

/**
 * Storage abstraction — swap implementations without touching calling code.
 * MVP: {@link LocalStorageServiceImpl}. Future: S3StorageServiceImpl.
 */
public interface StorageService {

    /**
     * Stores a file and returns the storage path (relative or absolute depending on implementation).
     *
     * @param inputStream  file data
     * @param fileName     original file name
     * @param subDirectory e.g. caseId as a string
     * @return storage path that can be recorded in the documents table
     */
    String store(InputStream inputStream, String fileName, String subDirectory);

    /**
     * Returns an InputStream to read a stored file.
     *
     * @param storagePath path returned by {@link #store}
     */
    InputStream retrieve(String storagePath);

    /**
     * Deletes the stored file.
     *
     * @param storagePath path returned by {@link #store}
     */
    void delete(String storagePath);
}
