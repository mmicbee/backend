package com.zone01kisumu.backend.courseLessonTests;

import com.zone01kisumu.backend.dto.CourseLessonDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.model.CourseLesson.RecordedMediaType;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseTopicRepository;
import com.zone01kisumu.backend.service.CourseLessonService;
import com.zone01kisumu.backend.service.LessonMetadataExtractor;
import com.zone01kisumu.backend.service.LessonMetadataExtractor.MediaMetadata;
import com.zone01kisumu.backend.storage.CourseFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class CourseLessonServiceTest {

    private CourseLessonRepository lessonRepo;
    private CourseRepository courseRepo;
    private CourseTopicRepository topicRepo;
    private LessonMetadataExtractor metadataExtractor;
    private CourseFileStorageService storageService;
    private CourseLessonService service;

    @BeforeEach
    void setUp() {
        lessonRepo = mock(CourseLessonRepository.class);
        courseRepo = mock(CourseRepository.class);
        topicRepo = mock(CourseTopicRepository.class);
        metadataExtractor = mock(LessonMetadataExtractor.class);
        storageService = mock(CourseFileStorageService.class);

        service = new CourseLessonService(
                lessonRepo, courseRepo, topicRepo, metadataExtractor, storageService);
    }

    // createLesson

    @Test
    void testCreateLesson_withFile() throws IOException {
        Course course = new Course();
        course.setId(1L);
        CourseTopic topic = new CourseTopic();
        topic.setId(2L);
        topic.setTotalExpectedLessons(1);

        when(courseRepo.existsById(anyLong())).thenReturn(true);
when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
when(topicRepo.findById(anyLong())).thenReturn(Optional.of(topic));


        MultipartFile file = mock(MultipartFile.class);
        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.VIDEO);
        metadata.setDuration(10);
        metadata.setDurationUnit("minutes");
        metadata.setFileSize(12345L);
        metadata.setContent(new byte[] { 1, 2, 3 });

        when(metadataExtractor.extractMetadata(file)).thenReturn(metadata);

        CourseLesson savedLesson = new CourseLesson();
        savedLesson.setId(5L);
        when(lessonRepo.save(any())).thenAnswer(invocation -> {
            CourseLesson l = invocation.getArgument(0);
            l.setId(5L);
            return l;
        });

        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Test Lesson");

        CourseLessonDTO result = service.createLesson(1L, 2L, dto, file);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("Test Lesson", result.getLessonName());
        assertEquals("VIDEO", result.getRecordedMediaType());

        // topic totalExpectedLessons incremented
        assertEquals(2, topic.getTotalExpectedLessons());
        verify(topicRepo).save(topic);
    }

    @Test
    void testCreateLesson_withRecordedLink() throws IOException {
        Course course = new Course();
        course.setId(1L);
        CourseTopic topic = new CourseTopic();
        topic.setId(2L);
        topic.setTotalExpectedLessons(0);

        when(courseRepo.existsById(anyLong())).thenReturn(true);
when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
when(topicRepo.findById(anyLong())).thenReturn(Optional.of(topic));


        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Link Lesson");
        dto.setRecordedLink("https://youtube.com/video");

        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.VIDEO);
        metadata.setDuration(0);
        metadata.setDurationUnit("minutes");
        metadata.setFileSize(0L);

        when(metadataExtractor.extractLinkMetadata(dto.getRecordedLink())).thenReturn(metadata);

        // Mock lessonRepo.save to return a CourseLesson
        when(lessonRepo.save(any())).thenAnswer(invocation -> {
            CourseLesson l = invocation.getArgument(0);
            l.setId(10L); // assign an ID
            return l;
        });

        CourseLessonDTO result = service.createLesson(1L, 2L, dto, null);

        assertEquals("VIDEO", result.getRecordedMediaType());
        assertEquals("https://youtube.com/video", result.getRecordedLink());
        assertNull(result.getLiveUrl());
        assertEquals(1, topic.getTotalExpectedLessons());
        assertEquals(10L, result.getId()); // confirm the ID from the mocked save
    }

    @Test
    void testCreateLesson_withLiveUrl() throws IOException {
        Course course = new Course();
        course.setId(1L);
        CourseTopic topic = new CourseTopic();
        topic.setId(2L);
        when(courseRepo.existsById(anyLong())).thenReturn(true);
when(courseRepo.findById(anyLong())).thenReturn(Optional.of(course));
when(topicRepo.findById(anyLong())).thenReturn(Optional.of(topic));


        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Live Lesson");
        dto.setLiveUrl("https://live.com/session");

        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.LIVE);
        metadata.setDuration(0);
        metadata.setDurationUnit("session");
        metadata.setFileSize(0L);
        when(metadataExtractor.extractLiveMetadata(dto.getLiveUrl())).thenReturn(metadata);
        // Mock lessonRepo.save to return a CourseLesson
        when(lessonRepo.save(any())).thenAnswer(invocation -> {
            CourseLesson l = invocation.getArgument(0);
            l.setId(10L); // assign an ID
            return l;
        });

        CourseLessonDTO result = service.createLesson(1L, 2L, dto, null);

        assertEquals("LIVE", result.getRecordedMediaType());
        assertEquals("https://live.com/session", result.getLiveUrl());
    }

    @Test
    void testCreateLesson_missingCourse_throws() {
        when(courseRepo.findById(1L)).thenReturn(Optional.empty());
        CourseLessonDTO dto = new CourseLessonDTO();
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.createLesson(1L, 1L, dto, null));
        assertTrue(ex.getMessage().contains("Course not found"));
    }

    @Test
    void testCreateLesson_missingTopic_throws() {
        Course course = new Course();
        course.setId(1L);
        when(courseRepo.existsById(1L)).thenReturn(true);
    when(courseRepo.findById(1L)).thenReturn(Optional.of(course));
    when(topicRepo.findById(1L)).thenReturn(Optional.empty());
        CourseLessonDTO dto = new CourseLessonDTO();
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.createLesson(1L, 1L, dto, null));
        assertTrue(ex.getMessage().contains("Course topic not found"));
    }

    // updateLesson

    @Test
    void testUpdateLesson_withFile() throws IOException {
        CourseLesson existing = new CourseLesson();
        existing.setId(1L);
        existing.setLessonName("Old Name");

        when(lessonRepo.findById(1L)).thenReturn(Optional.of(existing));

        MultipartFile file = mock(MultipartFile.class);
        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.VIDEO);
        metadata.setDuration(5);
        metadata.setDurationUnit("minutes");
        metadata.setFileSize(123L);
        metadata.setContent(new byte[] { 1, 2 });

        when(metadataExtractor.extractMetadata(file)).thenReturn(metadata);
        when(lessonRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Updated");

        CourseLessonDTO result = service.updateLesson(1L, dto, file);
        assertEquals("Updated", result.getLessonName());
        assertEquals("VIDEO", result.getRecordedMediaType());
    }

    @Test
    void testUpdateLesson_withRecordedLink() throws IOException {
        CourseLesson existing = new CourseLesson();
        existing.setId(2L);

        when(lessonRepo.findById(2L)).thenReturn(Optional.of(existing));

        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Link");
        dto.setRecordedLink("https://youtu.be/123");

        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.VIDEO);
        metadata.setDuration(0);
        metadata.setDurationUnit("minutes");
        metadata.setFileSize(0L);
        when(metadataExtractor.extractLinkMetadata(dto.getRecordedLink())).thenReturn(metadata);
        when(lessonRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        CourseLessonDTO result = service.updateLesson(2L, dto, null);
        assertEquals("VIDEO", result.getRecordedMediaType());
        assertEquals("https://youtu.be/123", result.getRecordedLink());
    }

    @Test
    void testUpdateLesson_withLiveUrl() throws IOException {
        CourseLesson existing = new CourseLesson();
        existing.setId(3L);

        when(lessonRepo.findById(3L)).thenReturn(Optional.of(existing));

        CourseLessonDTO dto = new CourseLessonDTO();
        dto.setLessonName("Live");
        dto.setLiveUrl("https://live.com");

        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaType(RecordedMediaType.LIVE);
        metadata.setDuration(0);
        metadata.setDurationUnit("session");
        metadata.setFileSize(0L);
        when(metadataExtractor.extractLiveMetadata(dto.getLiveUrl())).thenReturn(metadata);
        when(lessonRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        CourseLessonDTO result = service.updateLesson(3L, dto, null);
        assertEquals("LIVE", result.getRecordedMediaType());
        assertEquals("https://live.com", result.getLiveUrl());
    }

    @Test
    void testUpdateLesson_notFound_throws() {
        when(lessonRepo.findById(1L)).thenReturn(Optional.empty());
        CourseLessonDTO dto = new CourseLessonDTO();
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateLesson(1L, dto, null));
        assertTrue(ex.getMessage().contains("Lesson not found"));
    }

    // deleteLesson

    @Test
    void testDeleteLesson_success() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(1L);
        CourseTopic topic = new CourseTopic();
        topic.setId(10L);
        topic.setTotalExpectedLessons(2);
        lesson.setTopic(topic);

        when(lessonRepo.findById(1L)).thenReturn(Optional.of(lesson));

        service.deleteLesson(1L);

        verify(lessonRepo).delete(lesson);
        assertEquals(1, topic.getTotalExpectedLessons());
        verify(topicRepo).save(topic);
    }

    @Test
    void testDeleteLesson_notFound_throws() {
        when(lessonRepo.findById(1L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.deleteLesson(1L));
        assertTrue(ex.getMessage().contains("Lesson not found"));
    }

    // get methods

    @Test
    void testGetAllLessons() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(1L);
        when(lessonRepo.findAll()).thenReturn(List.of(lesson));

        List<CourseLessonDTO> result = service.getAllLessons();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetLessonById_found() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(2L);
        when(lessonRepo.findById(2L)).thenReturn(Optional.of(lesson));

        CourseLessonDTO dto = service.getLessonById(2L);
        assertEquals(2L, dto.getId());
    }

    @Test
    void testGetLessonById_notFound() {
        when(lessonRepo.findById(3L)).thenReturn(Optional.empty());
        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.getLessonById(3L));
        assertTrue(ex.getMessage().contains("Lesson not found"));
    }

    @Test
    void testGetLessonsByCourseId() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(5L);
        when(lessonRepo.findByCourseId(10L)).thenReturn(List.of(lesson));

        List<CourseLessonDTO> list = service.getLessonsByCourseId(10L);
        assertEquals(1, list.size());
        assertEquals(5L, list.get(0).getId());
    }

    @Test
    void testGetLessonsByTopicId() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(6L);
        when(lessonRepo.findByTopicId(20L)).thenReturn(List.of(lesson));

        List<CourseLessonDTO> list = service.getLessonsByTopicId(20L);
        assertEquals(1, list.size());
        assertEquals(6L, list.get(0).getId());
    }

    @Test
    void testGetLessonContent_found() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(1L);
        lesson.setRecordedMedia(new byte[] { 1, 2, 3 });
        when(lessonRepo.findById(1L)).thenReturn(Optional.of(lesson));

        byte[] content = service.getLessonContent(1L);
        assertArrayEquals(new byte[] { 1, 2, 3 }, content);
    }

    @Test
    void testGetLessonContent_noMedia_throws() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(1L);
        lesson.setRecordedMedia(null);
        when(lessonRepo.findById(1L)).thenReturn(Optional.of(lesson));

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> service.getLessonContent(1L));
        assertTrue(ex.getMessage().contains("has no recorded content"));
    }
}
