package com.zone01kisumu.backend.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zone01kisumu.backend.dto.CourseLessonDTO;
import com.zone01kisumu.backend.model.Course;
import com.zone01kisumu.backend.model.CourseLesson;
import com.zone01kisumu.backend.model.CourseTopic;
import com.zone01kisumu.backend.repository.CourseLessonRepository;
import com.zone01kisumu.backend.repository.CourseRepository;
import com.zone01kisumu.backend.repository.CourseTopicRepository;
import com.zone01kisumu.backend.service.LessonMetadataExtractor.MediaMetadata;
import com.zone01kisumu.backend.storage.CourseFileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseLessonService {

    private final CourseLessonRepository courseLessonRepository;
    private final CourseRepository courseRepository;
    private final CourseTopicRepository courseTopicRepository;
    private final LessonMetadataExtractor metadataExtractor;
    private final CourseFileStorageService courseFileStorageService;

    private static final String LESSON_NOT_FOUND = "Lesson not found with ID: ";
    private static final String COURSE_NOT_FOUND = "Course not found with ID: ";
    private static final String TOPIC_NOT_FOUND = "Course topic not found with ID: ";

    // Create a new lesson with automatic metadata extraction
     // User only needs to provide: lessonName, and ONE of (file, recordedLink, liveUrl)
    public CourseLessonDTO createLesson(Long courseId, Long topicId, CourseLessonDTO dto, 
                                       MultipartFile file) throws IOException {
        // Validate course and topic
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException(COURSE_NOT_FOUND + courseId);
        }
        CourseTopic topic = courseTopicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException(TOPIC_NOT_FOUND + topicId));

        // Create lesson entity
        CourseLesson lesson = new CourseLesson();
        lesson.setCourseId(courseId);
        lesson.setTopicId(topicId);
        lesson.setLessonName(dto.getLessonName());

        // Process based on input type and extract metadata automatically
        MediaMetadata metadata = processLessonContent(file, dto, lesson);

        if (file != null && !file.isEmpty()) {
            String storageKey = courseFileStorageService.store(file, "course-lessons/" + courseId + "/" + topicId);
            lesson.setRecordedLink(courseFileStorageService.resolvePublicUrl(storageKey));
        }

        // Set extracted metadata
        lesson.setRecordedMediaType(metadata.getMediaType());
        lesson.setDuration(metadata.getDuration());
        lesson.setDurationUnit(metadata.getDurationUnit());
        lesson.setFileSize(metadata.getFileSize());

        if (metadata.getContent() != null) {
            lesson.setRecordedMedia(metadata.getContent());
        }

        // Save lesson
        CourseLesson savedLesson = courseLessonRepository.save(lesson);

        // Update duration for the topic and the course safely
        int lessonDuration = (savedLesson.getDuration() != null) ? savedLesson.getDuration() : 0;
        int currentTopicDuration = (topic.getDuration() != null) ? topic.getDuration() : 0;
        topic.setDuration(currentTopicDuration + lessonDuration);
        Integer currentCount = topic.getTotalExpectedLessons();
        topic.setTotalExpectedLessons((currentCount != null ? currentCount : 0) + 1);
        courseTopicRepository.save(topic);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException(COURSE_NOT_FOUND + courseId));
        int currentCourseDuration = (course.getDuration() != null) ? course.getDuration() : 0;
        course.setDuration(currentCourseDuration + lessonDuration);
        courseRepository.save(course);


        log.info("Created {} lesson (ID={}) with duration={} {}, size={} bytes for Topic ID={}",
                savedLesson.getRecordedMediaType(), savedLesson.getId(), 
                savedLesson.getDuration(), savedLesson.getDurationUnit(),
                savedLesson.getFileSize(), topicId);

        return mapToDTO(savedLesson);
    }

    //Update existing lesson with automatic metadata re-extraction
    public CourseLessonDTO updateLesson(Long id, CourseLessonDTO dto, MultipartFile newFile) 
            throws IOException {
        CourseLesson existingLesson = courseLessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(LESSON_NOT_FOUND + id));

        // Update lesson name
        existingLesson.setLessonName(dto.getLessonName());

        // Process new content if provided
        if (newFile != null && !newFile.isEmpty()) {
            MediaMetadata metadata = metadataExtractor.extractMetadata(newFile);
            existingLesson.setRecordedMedia(metadata.getContent());
            existingLesson.setRecordedMediaType(metadata.getMediaType());
            existingLesson.setDuration(metadata.getDuration());
            existingLesson.setDurationUnit(metadata.getDurationUnit());
            existingLesson.setFileSize(metadata.getFileSize());
            existingLesson.setRecordedLink(null);
            existingLesson.setLiveUrl(null);

            String storagePath = "course-lessons/" + existingLesson.getCourseId()
                    + "/" + existingLesson.getTopicId();
            String storageKey = courseFileStorageService.store(newFile, storagePath);
            existingLesson.setRecordedLink(courseFileStorageService.resolvePublicUrl(
                    storageKey));
        } else if (dto.getRecordedLink() != null && !dto.getRecordedLink().isEmpty()) {
            MediaMetadata metadata = metadataExtractor.extractLinkMetadata(dto.getRecordedLink());
            existingLesson.setRecordedLink(dto.getRecordedLink());
            existingLesson.setRecordedMediaType(metadata.getMediaType());
            existingLesson.setDuration(metadata.getDuration());
            existingLesson.setDurationUnit(metadata.getDurationUnit());
            existingLesson.setFileSize(0L);
            existingLesson.setRecordedMedia(null);
            existingLesson.setLiveUrl(null);
        } else if (dto.getLiveUrl() != null && !dto.getLiveUrl().isEmpty()) {
            MediaMetadata metadata = metadataExtractor.extractLiveMetadata(dto.getLiveUrl());
            existingLesson.setLiveUrl(dto.getLiveUrl());
            existingLesson.setRecordedMediaType(metadata.getMediaType());
            existingLesson.setDuration(metadata.getDuration());
            existingLesson.setDurationUnit(metadata.getDurationUnit());
            existingLesson.setFileSize(0L);
            existingLesson.setRecordedMedia(null);
            existingLesson.setRecordedLink(null);
        }

        CourseLesson updatedLesson = courseLessonRepository.save(existingLesson);
        log.info("Updated lesson (ID={})", updatedLesson.getId());
        
        return mapToDTO(updatedLesson);
    }

    //Delete lesson and update topic count
    public void deleteLesson(Long lessonId) {
        CourseLesson lesson = courseLessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException(LESSON_NOT_FOUND + lessonId));

        CourseTopic topic = lesson.getTopic();
        courseLessonRepository.delete(lesson);

        if (topic != null) {
            // Decrement expected lessons safely
            Integer current = topic.getTotalExpectedLessons();
            topic.setTotalExpectedLessons(Math.max(0, (current != null ? current - 1 : 0)));
            int lessonDuration = (lesson.getDuration() != null) ? lesson.getDuration() : 0;
            int currentTopicDuration = (topic.getDuration() != null) ? topic.getDuration() : 0;
            topic.setDuration(Math.max(0, currentTopicDuration - lessonDuration));
            courseTopicRepository.save(topic);

            log.info("Deleted lesson (ID={}) and decremented totalExpectedLessons for Topic ID={}",
                    lessonId, topic.getId());
        }
    }

    //Get all lessons
    @Transactional(readOnly = true)
    public List<CourseLessonDTO> getAllLessons() {
        return courseLessonRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Get lesson by ID
    @Transactional(readOnly = true)
    public CourseLessonDTO getLessonById(Long id) {
        CourseLesson lesson = courseLessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(LESSON_NOT_FOUND + id));
        return mapToDTO(lesson);
    }

    //Get lessons by course ID
    @Transactional(readOnly = true)
    public List<CourseLessonDTO> getLessonsByCourseId(Long courseId) {
        return courseLessonRepository.findByCourseId(courseId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Get lessons by topic ID
    @Transactional(readOnly = true)
    public List<CourseLessonDTO> getLessonsByTopicId(Long topicId) {
        return courseLessonRepository.findByTopicId(topicId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    //Get recorded media content
    @Transactional(readOnly = true)
    public byte[] getLessonContent(Long id) {
        CourseLesson lesson = courseLessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(LESSON_NOT_FOUND + id));

        if (lesson.getRecordedMedia() != null) {
            return lesson.getRecordedMedia();
        }

        if (lesson.getRecordedLink() != null && !lesson.getRecordedLink().isBlank()) {
            String storageKey = courseFileStorageService.extractStorageKeyFromUrl(lesson.getRecordedLink());
            if (storageKey != null && !storageKey.isBlank()) {
                try {
                    return courseFileStorageService.read(storageKey);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Unable to read lesson content from storage for lesson ID " + id, e);
                }
            }
        }

        throw new IllegalArgumentException("Lesson with ID " + id + " has no recorded content.");
    }


    //Process lesson content and extract metadata automatically
    private MediaMetadata processLessonContent(MultipartFile file, CourseLessonDTO dto, 
                                               CourseLesson lesson) throws IOException {
        MediaMetadata metadata;

        // Priority: file > recordedLink > liveUrl
        if (file != null && !file.isEmpty()) {
            metadata = metadataExtractor.extractMetadata(file);
            // Don't set URLs if file is provided
            lesson.setRecordedLink(null);
            lesson.setLiveUrl(null);
        } else if (dto.getRecordedLink() != null && !dto.getRecordedLink().isEmpty()) {
            metadata = metadataExtractor.extractLinkMetadata(dto.getRecordedLink());
            lesson.setRecordedLink(dto.getRecordedLink());
            lesson.setLiveUrl(null);
        } else if (dto.getLiveUrl() != null && !dto.getLiveUrl().isEmpty()) {
            metadata = metadataExtractor.extractLiveMetadata(dto.getLiveUrl());
            lesson.setLiveUrl(dto.getLiveUrl());
            lesson.setRecordedLink(null);
        } else {
            throw new IllegalArgumentException(
                "Must provide either a file, recorded link, or live URL for the lesson");
        }

        return metadata;
    }

    //Map entity to DTO
    private CourseLessonDTO mapToDTO(CourseLesson lesson) {
        return CourseLessonDTO.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourseId())
                .topicId(lesson.getTopicId())
                .lessonName(lesson.getLessonName())
                .recordedMediaType(lesson.getRecordedMediaType() != null ? 
                        lesson.getRecordedMediaType().name() : null)
                .recordedLink(lesson.getRecordedLink())
                .liveUrl(lesson.getLiveUrl())
                .lessonDate(lesson.getLessonDate())
                .duration(lesson.getDuration())
                .durationUnit(lesson.getDurationUnit())
                .fileSize(lesson.getFileSize())
                .build();
    }
}