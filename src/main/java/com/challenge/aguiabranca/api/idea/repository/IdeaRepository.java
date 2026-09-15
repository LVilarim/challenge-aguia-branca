package com.challenge.aguiabranca.api.idea.repository;

import com.challenge.aguiabranca.api.idea.domain.IdeaDocument;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdeaRepository extends MongoRepository<IdeaDocument, String> {
    Optional<IdeaDocument> findByIdAndAuthorUserId(String id, String authorUserId);
    Page<IdeaDocument> findByAuthorUserId(String authorUserId, Pageable pageable);
}

