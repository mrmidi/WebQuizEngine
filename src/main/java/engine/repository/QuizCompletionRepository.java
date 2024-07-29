package engine.repository;

import engine.model.QuizCompletion;
import engine.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizCompletionRepository extends JpaRepository<QuizCompletion, Long> {

    Page<QuizCompletion> findByUserOrderByCompletedAtDesc(User user, Pageable pageable);
}