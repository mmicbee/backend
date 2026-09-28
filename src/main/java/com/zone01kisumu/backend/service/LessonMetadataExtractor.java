package com.zone01kisumu.backend.service;

import com.zone01kisumu.backend.model.CourseLesson.RecordedMediaType;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LessonMetadataExtractor {

    @Data
    public static class MediaMetadata {
        private RecordedMediaType mediaType;
        private Integer duration;
        private String durationUnit;
        private Long fileSize;
        private byte[] content;
    }

    //Extracts metadata from uploaded file
    public MediaMetadata extractMetadata(MultipartFile file) throws IOException {
        MediaMetadata metadata = new MediaMetadata();
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        metadata.setContent(file.getBytes());
        metadata.setFileSize(file.getSize());
        
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        // Determine media type and extract specific metadata
        if (isVideoFile(contentType, fileName)) {
            metadata.setMediaType(RecordedMediaType.VIDEO);
            extractVideoMetadata(file, metadata);
        } else if (contentType != null && contentType.equals("application/pdf")) {
            metadata.setMediaType(RecordedMediaType.PDF);
            extractPdfMetadata(file, metadata);
        } else if (isSlideFile(contentType, fileName)) {
            metadata.setMediaType(RecordedMediaType.SLIDES);
            extractSlideMetadata(file, metadata);
        } else {
            metadata.setMediaType(RecordedMediaType.OTHER);
            metadata.setDuration(0);
            metadata.setDurationUnit("bytes");
        }
        
        log.info("Extracted metadata: type={}, duration={} {}, size={} bytes", 
                metadata.getMediaType(), metadata.getDuration(), 
                metadata.getDurationUnit(), metadata.getFileSize());
        
        return metadata;
    }

    /**
     * Extracts metadata from live URL
     * 
     * @param liveUrl URL of the live session
     * @return MediaMetadata with default values
     */
    public MediaMetadata extractLiveMetadata(String liveUrl) {
        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.LIVE);
        metadata.setDuration(0);
        metadata.setDurationUnit("session");
        metadata.setFileSize(0L);
        return metadata;
    }

    /**
     * Extracts metadata from recorded link
     * 
     * @param recordedLink Recorded link URL
     * @return MediaMetadata with default values
     */
    public MediaMetadata extractLinkMetadata(String recordedLink) {
        MediaMetadata metadata = new MediaMetadata();
        
        // Try to determine type from URL
        if (recordedLink.contains("youtube.com") || recordedLink.contains("youtu.be") || 
            recordedLink.contains("vimeo.com")) {
            metadata.setMediaType(RecordedMediaType.VIDEO);
            metadata.setDurationUnit("seconds");
        } else if (recordedLink.toLowerCase().endsWith(".pdf")) {
            metadata.setMediaType(RecordedMediaType.PDF);
            metadata.setDurationUnit("pages");
        } else {
            metadata.setMediaType(RecordedMediaType.OTHER);
            metadata.setDurationUnit("link");
        }
        
        metadata.setDuration(0);
        metadata.setFileSize(0L);
        return metadata;
    }

    private void extractVideoMetadata(MultipartFile file, MediaMetadata metadata) throws IOException {
        File tempFile = null;
        try {
            // JAVE requires a physical file, so we create a temporary one
            tempFile = File.createTempFile("video-", "-" + file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            // Use JAVE to get multimedia information
            MultimediaObject multimediaObject = new MultimediaObject(tempFile);
            MultimediaInfo info = multimediaObject.getInfo();

            // Duration is in milliseconds, convert to seconds
            long durationMs = info.getDuration();
            long durationSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMs);

            metadata.setDuration((int) durationSeconds);
            metadata.setDurationUnit("seconds");

            log.info("Precise video metadata extracted: {} seconds", durationSeconds);

        } catch (EncoderException e) {
            log.error("Error extracting precise video metadata with JAVE", e);
            // Fallback to estimation if JAVE fails
            long fileSizeMB = file.getSize() / (1024 * 1024);
            metadata.setDuration((int) Math.max(1, fileSizeMB));
            metadata.setDurationUnit("minutes (estimated)");
            log.warn("Fell back to estimated duration: {} minutes", metadata.getDuration());
        } finally {
            // Clean up the temporary file
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    log.warn("Could not delete temporary file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private void extractPdfMetadata(MultipartFile file, MediaMetadata metadata) throws IOException {
    byte[] bytes = file.getBytes();

    try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(bytes))) {

        int pageCount = document.getNumberOfPages();
        metadata.setDuration(pageCount*60);//to be in seconds
        metadata.setDurationUnit("seconds");

        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        int wordCount = countWords(text);

        log.info("PDF metadata extracted: {} pages, ~{} words", pageCount, wordCount);

    } catch (IOException e) {
        log.error("Error extracting PDF metadata", e);
        metadata.setDuration(0);
        metadata.setDurationUnit("pages");
    }
}


    private void extractSlideMetadata(MultipartFile file, MediaMetadata metadata) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(file.getBytes()))) {
            int slideCount = ppt.getSlides().size();
            metadata.setDuration(slideCount*60); //to be in seconds
            metadata.setDurationUnit("seconds");
            
            log.info("Slide metadata extracted: {} slides", slideCount);
        } catch (Exception e) {
            log.error("Error extracting slide metadata", e);
            metadata.setDuration(0);
            metadata.setDurationUnit("slides");
        }
    }

    private boolean isVideoFile(String contentType, String fileName) {
        if (contentType != null && contentType.startsWith("video/")) {
            return true;
        }
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            return lower.endsWith(".mp4") || lower.endsWith(".mov");
        }
        return false;
    }

    private boolean isSlideFile(String contentType, String fileName) {
        if (contentType != null) {
            return contentType.contains("presentation") || 
                   contentType.contains("powerpoint") ||
                   contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") ||
                   contentType.equals("application/vnd.ms-powerpoint");
        }
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            return lower.endsWith(".ppt") || lower.endsWith(".pptx");
        }
        return false;
    }

    private int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        String[] words = text.trim().split("\\s+");
        return words.length;
    }
}