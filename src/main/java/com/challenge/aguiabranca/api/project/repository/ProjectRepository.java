package com.challenge.aguiabranca.api.project.repository;

import com.challenge.aguiabranca.api.project.domain.ProjectDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<ProjectDocument, String> {
    boolean existsBySourceIdeaId(String sourceIdeaId);
    Optional<ProjectDocument> findBySourceIdeaId(String sourceIdeaId);
}

