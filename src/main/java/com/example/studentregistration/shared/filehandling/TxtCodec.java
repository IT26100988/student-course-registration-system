
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class TxtCodec {

    private TxtCodec() {
        // Utility class: all methods are static.
    }

    // Convert one Java value into a safe TXT field.
    public static String encodeField(String value) {

        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    // Convert one encoded TXT field back into a Java value.
    public static String decodeField(String encoded)
            throws FileStorageException {

        if (encoded == null) {
            throw new FileStorageException(
                    "TXT field cannot be null");
        }

        try {
            String decoded = URLDecoder.decode(
                    encoded, StandardCharsets.UTF_8);

            // Validate the frozen encoding format.
            if (!encodeField(decoded).equals(encoded)) {
                throw new FileStorageException(
                        "Invalid TXT field encoding");
            }

            // An empty optional field represents null.
            return decoded.isEmpty() ? null : decoded;

        } catch (IllegalArgumentException e) {
            throw new FileStorageException(
                    "Malformed TXT percent encoding", e);
        }
    }

    // Combine multiple fields into one TXT record.
    public static String encodeRecord(List<String> fields)
            throws FileStorageException {

        if (fields == null || fields.isEmpty()) {
            throw new FileStorageException(
                    "Record must contain fields");
        }

        ArrayList<String> encoded = new ArrayList<>();

        for (String field : fields) {
            encoded.add(encodeField(field));
        }

        return String.join("|", encoded);
    }

    // Split and decode one TXT record.
    public static ArrayList<String> decodeRecord(
            String line, int expectedFieldCount)
            throws FileStorageException {

        if (expectedFieldCount <= 0 || line == null
                || line.isEmpty()
                || line.contains("\n")
                || line.contains("\r")) {

            throw new FileStorageException(
                    "Invalid TXT record");
        }

        String[] parts = line.split("\\|", -1);

        if (parts.length != expectedFieldCount) {
            throw new FileStorageException(
                    "Incorrect TXT field count");
        }

        ArrayList<String> decoded = new ArrayList<>();

        for (String part : parts) {
            decoded.add(decodeField(part));
        }

        return decoded;
    }
}
