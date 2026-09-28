package com.zone01kisumu.backend.courseLessonTests;

import com.zone01kisumu.backend.model.CourseLesson.RecordedMediaType;
import com.zone01kisumu.backend.service.LessonMetadataExtractor;
import com.zone01kisumu.backend.service.LessonMetadataExtractor.MediaMetadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

class LessonMetadataExtractorTest {

    private LessonMetadataExtractor extractor;

    @BeforeEach
    void setup() {
        extractor = new LessonMetadataExtractor();
    }

    // TEST: Empty or null file
    @Test
    void testExtractMetadata_FileEmpty_ThrowsException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> extractor.extractMetadata(file));
    }

    // TEST: Video File Detection
    @Test
    void testExtractMetadata_Video() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        byte[] content = "video-bytes".getBytes();

        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(content);
        when(file.getSize()).thenReturn(5L * 1024 * 1024); // 5MB → 5 min estimate
        when(file.getContentType()).thenReturn("video/mp4");
        when(file.getOriginalFilename()).thenReturn("video.mp4");

        MediaMetadata metadata = extractor.extractMetadata(file);

        assertEquals(RecordedMediaType.VIDEO, metadata.getMediaType());
        assertEquals(5, metadata.getDuration());
        assertEquals("minutes (estimated)", metadata.getDurationUnit());
        assertEquals(content.length, metadata.getContent().length);
    }

    // TEST: PDF File Detection
    @Test
    void testExtractMetadata_Pdf() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        byte[] pdfBytes = new byte[] { 1, 2, 3 };

        // PDFBox is complex; we test basic behavior only.
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(pdfBytes);
        when(file.getSize()).thenReturn(300L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("file.pdf");

        // We can't mock PDFBox internals here, but catche exceptions
        // safely.
        MediaMetadata metadata = extractor.extractMetadata(file);

        assertEquals(RecordedMediaType.PDF, metadata.getMediaType());
        assertNotNull(metadata.getDurationUnit()); // pages
        assertNotNull(metadata.getDuration()); // 0 or fallback
        assertEquals(pdfBytes.length, metadata.getContent().length);
    }

    // TEST: Slide File Detection
    @Test
    void testExtractMetadata_Slides() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        byte[] slideBytes = new byte[] { 10, 20 };

        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(slideBytes);
        when(file.getSize()).thenReturn(200L);
        when(file.getContentType()).thenReturn(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        when(file.getOriginalFilename()).thenReturn("slides.pptx");

        MediaMetadata metadata = extractor.extractMetadata(file);

        assertEquals(RecordedMediaType.SLIDES, metadata.getMediaType());
        assertEquals("slides", metadata.getDurationUnit());
        assertNotNull(metadata.getDuration());
    }

    // TEST: Unknown File → OTHER
    @Test
    void testExtractMetadata_Other() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        byte[] content = "unknown".getBytes();

        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(content);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getOriginalFilename()).thenReturn("notes.txt");

        MediaMetadata metadata = extractor.extractMetadata(file);

        assertEquals(RecordedMediaType.OTHER, metadata.getMediaType());
        assertEquals(0, metadata.getDuration());
        assertEquals("bytes", metadata.getDurationUnit());
    }

    // TEST: Live URL metadata
    @Test
    void testExtractLiveMetadata() {
        MediaMetadata metadata = extractor.extractLiveMetadata("https://zoom.com/live/123");

        assertEquals(RecordedMediaType.LIVE, metadata.getMediaType());
        assertEquals(0, metadata.getDuration());
        assertEquals("session", metadata.getDurationUnit());
        assertEquals(0L, metadata.getFileSize());
    }

    // TEST: Link Metadata (YouTube)
    @Test
    void testExtractLinkMetadata_VideoLink() {
        MediaMetadata metadata = extractor.extractLinkMetadata("https://youtube.com/watch?v=abc");

        assertEquals(RecordedMediaType.VIDEO, metadata.getMediaType());
        assertEquals("seconds", metadata.getDurationUnit());
    }

    // TEST: Link Metadata (PDF link)
    @Test
    void testExtractLinkMetadata_PdfLink() {
        MediaMetadata metadata = extractor.extractLinkMetadata("https://example.com/file.pdf");

        assertEquals(RecordedMediaType.PDF, metadata.getMediaType());
        assertEquals("pages", metadata.getDurationUnit());
    }

    // TEST: Link Metadata (Other link)
    @Test
    void testExtractLinkMetadata_OtherLink() {
        MediaMetadata metadata = extractor.extractLinkMetadata("https://example.com/index.html");

        assertEquals(RecordedMediaType.OTHER, metadata.getMediaType());
        assertEquals("link", metadata.getDurationUnit());
    }

    // TEST: countWords() private method
    @Test
    void testCountWords_Null_ReturnsZero() throws Exception {
        int result = invokeCountWords(null);
        assertEquals(0, result);
    }

    @Test
    void testCountWords_Empty_ReturnsZero() throws Exception {
        int result = invokeCountWords("   ");
        assertEquals(0, result);
    }

    @Test
    void testCountWords_SingleWord() throws Exception {
        int result = invokeCountWords("Hello");
        assertEquals(1, result);
    }

    @Test
    void testCountWords_MultipleWords() throws Exception {
        int result = invokeCountWords("Hello world this is a test");
        assertEquals(6, result);
    }

    @Test
    void testCountWords_WithExtraSpaces() throws Exception {
        int result = invokeCountWords("  Hello   world   test  ");
        assertEquals(3, result);
    }

    // Utility method to call private method via reflection
    private int invokeCountWords(String text) throws Exception {
        var method = LessonMetadataExtractor.class.getDeclaredMethod("countWords", String.class);
        method.setAccessible(true);
        return (int) method.invoke(extractor, text);
    }

    @Test
void testExtractPdfMetadata() throws Exception {
    // Create an in-memory PDF with 2 pages and text
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (PDDocument doc = new PDDocument()) {
        doc.addPage(new PDPage());
        doc.addPage(new PDPage());
        new PDFTextStripper(); // ensures stripper loads
        doc.save(out);
    }

    byte[] pdfBytes = out.toByteArray();

    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getBytes()).thenReturn(pdfBytes);
    when(mockFile.getSize()).thenReturn((long) pdfBytes.length);
    when(mockFile.getContentType()).thenReturn("application/pdf");
    when(mockFile.getOriginalFilename()).thenReturn("test.pdf");

    LessonMetadataExtractor.MediaMetadata metadata =
            extractor.extractMetadata(mockFile);

    assertEquals(RecordedMediaType.PDF, metadata.getMediaType());
    assertEquals(120, metadata.getDuration()); // 120 seconds for 2 pages
    assertEquals("seconds", metadata.getDurationUnit());
    assertEquals(pdfBytes.length, metadata.getFileSize());
}

@Test
void testExtractSlideMetadata() throws Exception {
    // Create an in-memory PPTX with 3 slides
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    XMLSlideShow ppt = new XMLSlideShow();
    ppt.createSlide();
    ppt.createSlide();
    ppt.createSlide();
    ppt.write(out);
    ppt.close();

    byte[] pptBytes = out.toByteArray();

    MultipartFile mockFile = mock(MultipartFile.class);
    when(mockFile.getBytes()).thenReturn(pptBytes);
    when(mockFile.getSize()).thenReturn((long) pptBytes.length);
    when(mockFile.getContentType()).thenReturn(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );
    when(mockFile.getOriginalFilename()).thenReturn("slides.pptx");

    LessonMetadataExtractor.MediaMetadata metadata =
            extractor.extractMetadata(mockFile);

    assertEquals(RecordedMediaType.SLIDES, metadata.getMediaType());
    assertEquals(180, metadata.getDuration());  
    assertEquals("seconds", metadata.getDurationUnit());
}

@Test
void testIsSlideFile_ByFilenameOnly() throws Exception {
    MultipartFile mockFile = mock(MultipartFile.class);

    when(mockFile.getBytes()).thenReturn(new byte[]{1});
    when(mockFile.getSize()).thenReturn(1L);
    when(mockFile.getContentType()).thenReturn(null);
    when(mockFile.getOriginalFilename()).thenReturn("presentation.pptx");

    LessonMetadataExtractor.MediaMetadata metadata =
            extractor.extractMetadata(mockFile);

    assertEquals(RecordedMediaType.SLIDES, metadata.getMediaType());
    assertEquals("slides", metadata.getDurationUnit());
}

}

