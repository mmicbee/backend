package com.zone01kisumu.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zone01kisumu.backend.model.PipelineExecutionLog;

/**
 * Spring Data JPA Repository for PipelineExecutionLog.
 */
@Repository
public interface PipelineExecutionLogRepository extends JpaRepository<PipelineExecutionLog, Long> {

    Optional<PipelineExecutionLog> findTopByPipelineNameOrderByStartTimeDesc(String pipelineName);

    List<PipelineExecutionLog> findTop10ByPipelineNameOrderByStartTimeDesc(String pipelineName);

    long countByPipelineNameAndExecutionStatus(
            String pipelineName, PipelineExecutionLog.PipelineStatus executionStatus);

    long countByPipelineName(String pipelineName);

    List<PipelineExecutionLog> findByPipelineNameOrderByStartTimeDesc(String pipelineName);
}
